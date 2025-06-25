# 🚀 블로그 퍼블릭 API 스트레스 테스트 가이드

## 📋 개요

이 스트레스 테스트 도구는 블로그 백엔드 API의 성능을 종합적으로 검증하기 위해 설계되었습니다. 실제 사용자 행동 패턴을 기반으로 한 다양한 시나리오를 제공합니다.

## 🎯 테스트 시나리오

### 1. **일반 방문자 시나리오** (`normal-visitor.js`)
- **대상**: 일반적인 블로그 방문자 행동 패턴
- **부하**: 20명 → 100명 (점진적 증가)
- **패턴**: 게시글 목록 70%, 상세보기 15%, 카테고리 10%, 댓글 5%

### 2. **댓글 중심 시나리오** (`comment-heavy.js`)
- **대상**: 댓글 작성/조회 집중 테스트
- **부하**: 30명 → 100명 (점진적 증가)
- **패턴**: 댓글 작성 60%, 댓글 조회 30%, 게시글 보기 10%

### 3. **스파이크 테스트** (`spike-test.js`)
- **대상**: 급격한 트래픽 증가 상황
- **부하**: 10명 → 300명 (급격한 증가)
- **패턴**: 빠른 읽기 작업 위주

## ⚡ 빠른 시작

### 1. 사전 준비

```bash
# 1. K6 설치 (macOS)
brew install k6

# 1. K6 설치 (Ubuntu)
sudo apt-get update && sudo apt-get install k6

# 2. 서버 실행
./gradlew bootRun

# 3. 서버 상태 확인
curl http://localhost:8080/categories
```

### 2. 테스트 실행

```bash
# 모든 테스트 자동 실행
./stress-test/run-tests.sh

# 개별 테스트 실행
k6 run stress-test/k6-scenarios/normal-visitor.js
k6 run stress-test/k6-scenarios/comment-heavy.js
k6 run stress-test/k6-scenarios/spike-test.js
```

### 3. 결과 확인

```bash
# 결과 파일 위치
ls -la stress-test/results/

# 실시간 모니터링
curl http://localhost:8080/actuator/health
```

## 📊 성능 목표

| 메트릭        | 목표          | 임계값        |
| ------------- | ------------- | ------------- |
| 게시글 목록   | < 200ms (95%) | < 500ms (99%) |
| 게시글 상세   | < 150ms (95%) | < 300ms (99%) |
| 댓글 조회     | < 100ms (95%) | < 200ms (99%) |
| 댓글 작성     | < 300ms (95%) | < 800ms (99%) |
| 카테고리 목록 | < 50ms (95%)  | < 100ms (99%) |
| 오류율        | < 1%          | < 5%          |
| 동시 사용자   | 200명+        | 500명+        |

## 🔍 모니터링

### 실시간 메트릭 확인

```bash
# HTTP 메트릭
curl -s http://localhost:8080/actuator/metrics/http.server.requests | jq

# JVM 메모리
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq

# 데이터베이스 연결 풀
curl -s http://localhost:8080/actuator/metrics/hikari.connections | jq
```

### 시스템 리소스 모니터링

```bash
# CPU/메모리 사용률
top -p $(pgrep -f java)

# 네트워크 연결
netstat -tulpn | grep :8080

# 로그 모니터링
tail -f logs/spring.log
```

## 🛠️ 커스터마이징

### 테스트 시나리오 수정

```javascript
// normal-visitor.js에서 부하 패턴 조정
export let options = {
  stages: [
    { duration: '2m', target: 50 },   // 50명으로 조정
    { duration: '5m', target: 50 },   // 유지 시간 조정
    // ...
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // 임계값 조정
    http_req_failed: ['rate<0.02'],   // 오류율 조정
  },
};
```

### 사용자 행동 패턴 조정

```javascript
// 시나리오 가중치 조정
const scenarios = [
  { weight: 80, action: 'getPostsList' },    // 80%로 증가
  { weight: 10, action: 'getPostDetail' },   // 10%로 감소
  { weight: 5, action: 'getCategories' },
  { weight: 5, action: 'getComments' }
];
```

## 📈 결과 분석

### 주요 확인 포인트

1. **응답 시간 분포**
   - 평균, 95%, 99% percentile 확인
   - 시간대별 응답 시간 추이

2. **처리량 (RPS)**
   - 초당 요청 처리 수
   - 피크 처리량 달성 여부

3. **오류율**
   - HTTP 4xx, 5xx 응답 비율
   - 오류 패턴 분석

4. **시스템 리소스**
   - CPU/메모리 사용률
   - 데이터베이스 연결 풀 상태

### 병목 지점 식별

```bash
# 느린 쿼리 확인
grep "slow query" logs/spring.log

# 메모리 부족 확인
grep "OutOfMemoryError" logs/spring.log

# 연결 풀 고갈 확인
grep "Connection pool" logs/spring.log
```

## 🔧 최적화 가이드

### 1. 데이터베이스 최적화

```sql
-- 인덱스 추가
CREATE INDEX idx_post_category_created ON post(category_id, created_at);
CREATE INDEX idx_comment_post_created ON comment(post_id, created_at);

-- 쿼리 분석
EXPLAIN ANALYZE SELECT * FROM post WHERE category_id = 1 ORDER BY created_at DESC LIMIT 10;
```

### 2. 캐싱 전략

```java
@Cacheable("categories")
public List<CategoryResponse> getCategories() {
    // 카테고리 목록 캐싱
}

@Cacheable(value = "posts", key = "#page + '-' + #size")
public PostListResponse getPostList(int page, int size, String sort) {
    // 게시글 목록 캐싱
}
```

### 3. 연결 풀 튜닝

```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
```

## 🚨 주의사항

1. **프로덕션 환경에서 테스트 금지**
   - 반드시 개발/스테이징 환경에서만 실행
   - 실제 사용자에게 영향을 주지 않도록 주의

2. **시스템 리소스 모니터링 필수**
   - 테스트 중 CPU/메모리 사용률 확인
   - 과부하 시 즉시 테스트 중단

3. **데이터 정합성 확인**
   - 테스트 후 데이터 무결성 검증
   - 필요시 테스트 데이터 정리

## 📞 문제 해결

### 자주 발생하는 문제

1. **K6 설치 문제**
   ```bash
   # macOS에서 권한 문제
   sudo chown -R $(whoami) /usr/local/share/zsh/site-functions
   
   # Linux에서 설치 실패
   curl -s https://dl.k6.io/key.gpg | sudo apt-key add -
   ```

2. **서버 연결 실패**
   ```bash
   # 서버 상태 확인
   curl -I http://localhost:8080/health
   
   # 포트 확인
   lsof -i :8080
   ```

3. **메모리 부족**
   ```bash
   # JVM 힙 크기 증가
   export JAVA_OPTS="-Xmx2g -Xms1g"
   ./gradlew bootRun
   ```

## 📚 추가 자료

- [K6 공식 문서](https://k6.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer 메트릭](https://micrometer.io/docs)
- [HikariCP 설정 가이드](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby) 