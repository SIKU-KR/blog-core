# 스트레스 테스트 모니터링 설정 가이드

## 📊 모니터링 도구 설정

### 1. Spring Boot Actuator 설정

`application.properties`에 다음 설정 추가:

```properties
# Actuator 엔드포인트 활성화
management.endpoints.web.exposure.include=health,metrics,info,prometheus
management.endpoint.health.show-details=always
management.endpoint.metrics.enabled=true

# 메트릭 수집 설정
management.metrics.web.server.request.autotime.enabled=true
management.metrics.jdbc.datasource.enabled=true
management.metrics.system.cpu.enabled=true
management.metrics.jvm.enabled=true

# HTTP 요청 메트릭 상세 정보
management.metrics.web.server.request.metric-name=http_server_requests
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.distribution.sla.http.server.requests=50ms,100ms,200ms,300ms,500ms,1s
```

### 2. 추가 의존성 (build.gradle)

```gradle
dependencies {
    // Actuator for monitoring
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Micrometer for metrics
    implementation 'io.micrometer:micrometer-registry-prometheus'
    
    // Optional: HikariCP metrics
    implementation 'com.zaxxer:HikariCP'
}
```

### 3. 커스텀 메트릭 추가

```java
@Component
public class CustomMetrics {
    
    private final Counter commentCreationCounter;
    private final Timer postRetrievalTimer;
    private final Gauge activeUsersGauge;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.commentCreationCounter = Counter.builder("blog.comments.created")
            .description("Number of comments created")
            .register(meterRegistry);
            
        this.postRetrievalTimer = Timer.builder("blog.posts.retrieval.time")
            .description("Post retrieval time")
            .register(meterRegistry);
            
        this.activeUsersGauge = Gauge.builder("blog.users.active")
            .description("Number of active users")
            .register(meterRegistry, this, CustomMetrics::getActiveUsers);
    }
    
    public void recordCommentCreation() {
        commentCreationCounter.increment();
    }
    
    public void recordPostRetrieval(Duration duration) {
        postRetrievalTimer.record(duration);
    }
    
    private double getActiveUsers() {
        // 활성 사용자 수 계산 로직
        return 0.0;
    }
}
```

## 🔍 시스템 모니터링

### 1. JVM 메모리 모니터링

```bash
# 힙 덤프 생성
jcmd <PID> GC.run_finalization
jcmd <PID> VM.gc
jmap -dump:format=b,file=heap.hprof <PID>

# 가비지 컬렉션 로그 설정 (JVM 옵션)
-XX:+UseG1GC
-XX:+PrintGC
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Xloggc:gc.log
```

### 2. 데이터베이스 연결 풀 모니터링

```java
@Component
@ConfigurationProperties("spring.datasource.hikari")
public class HikariMetrics {
    
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        HikariDataSource dataSource = event.getApplicationContext()
            .getBean(HikariDataSource.class);
            
        // 연결 풀 메트릭 등록
        Gauge.builder("hikari.connections.active")
            .register(Metrics.globalRegistry, dataSource.getHikariPoolMXBean()::getActiveConnections);
            
        Gauge.builder("hikari.connections.idle")
            .register(Metrics.globalRegistry, dataSource.getHikariPoolMXBean()::getIdleConnections);
            
        Gauge.builder("hikari.connections.total")
            .register(Metrics.globalRegistry, dataSource.getHikariPoolMXBean()::getTotalConnections);
    }
}
```

### 3. 시스템 리소스 모니터링

```bash
# CPU 사용률 모니터링
top -p $(pgrep -f java)

# 메모리 사용률 모니터링
ps -p $(pgrep -f java) -o pid,vsz,rss,pmem,comm

# 네트워크 연결 모니터링
netstat -tulpn | grep :8080

# 파일 디스크립터 사용량
lsof -p $(pgrep -f java) | wc -l
```

## 📈 Prometheus + Grafana 설정 (선택사항)

### 1. Docker Compose 설정

```yaml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-storage:/var/lib/grafana

volumes:
  grafana-storage:
```

### 2. Prometheus 설정 (prometheus.yml)

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'blog-backend'
    static_configs:
      - targets: ['host.docker.internal:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
```

## 🚨 알림 설정

### 1. 임계값 정의

```yaml
# alert-rules.yml
groups:
  - name: blog-backend-alerts
    rules:
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le)) > 0.5
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is above 500ms for 2 minutes"

      - alert: HighErrorRate
        expr: sum(rate(http_server_requests_total{status=~"5.."}[5m])) / sum(rate(http_server_requests_total[5m])) > 0.05
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5% for 1 minute"

      - alert: DatabaseConnectionPoolExhaustion
        expr: hikari_connections_active / hikari_connections_max > 0.9
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Database connection pool near exhaustion"
          description: "Connection pool is 90% utilized"
```

### 2. Discord 웹훅 알림 (기존 코드 활용)

```java
@Component
public class AlertManager {
    
    private final DiscordWebhookCreator discord;
    private final MeterRegistry meterRegistry;
    
    @EventListener
    @Async
    public void handleHighLoad(ApplicationEvent event) {
        // 높은 부하 감지 시 알림
        if (isHighLoadDetected()) {
            discord.sendMessage("🚨 높은 부하가 감지되었습니다. 시스템을 점검해주세요.");
        }
    }
    
    private boolean isHighLoadDetected() {
        // 메트릭을 기반으로 높은 부하 감지 로직
        return false;
    }
}
```

## 📊 대시보드 KPI

### 1. 핵심 성능 지표

- **응답 시간**: 평균, 95%, 99% percentile
- **처리량**: RPS (Requests Per Second)
- **오류율**: 4xx, 5xx 응답 비율
- **가용성**: 업타임 비율

### 2. 시스템 리소스 지표

- **CPU 사용률**: 전체 및 애플리케이션별
- **메모리 사용률**: 힙, 논힙 메모리
- **GC 성능**: GC 빈도, 일시정지 시간
- **데이터베이스**: 연결 풀, 쿼리 성능

### 3. 비즈니스 지표

- **댓글 생성률**: 시간당 댓글 수
- **게시글 조회수**: 인기 게시글 트렌드
- **카테고리별 트래픽**: 카테고리별 접근 패턴

## 🔧 실시간 모니터링 명령어

```bash
# 실시간 로그 모니터링
tail -f logs/spring.log | grep "ERROR\|WARN"

# 실시간 메트릭 확인
curl -s http://localhost:8080/actuator/metrics/http.server.requests | jq

# 실시간 헬스 체크
watch -n 5 "curl -s http://localhost:8080/actuator/health | jq"

# 데이터베이스 연결 모니터링
watch -n 2 "curl -s http://localhost:8080/actuator/metrics/hikari.connections.active | jq"
``` 