# 블로그 퍼블릭 API 스트레스 테스트 시나리오

## 📊 테스트 도구 추천

### 1. K6 (추천)
```javascript
// scenario-1-normal-visitor.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const errorRate = new Rate('errors');

export let options = {
  stages: [
    { duration: '2m', target: 100 },   // 2분간 100명까지 증가
    { duration: '5m', target: 100 },   // 5분간 100명 유지
    { duration: '2m', target: 200 },   // 2분간 200명까지 증가
    { duration: '5m', target: 200 },   // 5분간 200명 유지
    { duration: '2m', target: 0 },     // 2분간 점진적 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이하
    http_req_failed: ['rate<0.05'],    // 오류율 5% 이하
  },
};

export default function() {
  // 70% - 게시글 목록 조회
  if (Math.random() < 0.7) {
    let page = Math.floor(Math.random() * 5); // 0-4 페이지
    let response = http.get(`${BASE_URL}/posts?page=${page}&size=10`);
    check(response, {
      'posts list status is 200': (r) => r.status === 200,
      'posts list response time < 500ms': (r) => r.timings.duration < 500,
    });
    errorRate.add(response.status !== 200);
  }
  
  // 15% - 특정 게시글 조회
  else if (Math.random() < 0.85) {
    let postId = Math.floor(Math.random() * 50) + 1; // 1-50 게시글
    let response = http.get(`${BASE_URL}/posts/${postId}`);
    check(response, {
      'post detail status is 200 or 404': (r) => r.status === 200 || r.status === 404,
      'post detail response time < 300ms': (r) => r.timings.duration < 300,
    });
    errorRate.add(response.status !== 200 && response.status !== 404);
  }
  
  // 10% - 카테고리 목록 조회
  else if (Math.random() < 0.95) {
    let response = http.get(`${BASE_URL}/categories`);
    check(response, {
      'categories status is 200': (r) => r.status === 200,
      'categories response time < 200ms': (r) => r.timings.duration < 200,
    });
    errorRate.add(response.status !== 200);
  }
  
  // 5% - 댓글 조회
  else {
    let postId = Math.floor(Math.random() * 50) + 1;
    let response = http.get(`${BASE_URL}/comments/${postId}`);
    check(response, {
      'comments status is 200 or 404': (r) => r.status === 200 || r.status === 404,
      'comments response time < 400ms': (r) => r.timings.duration < 400,
    });
    errorRate.add(response.status !== 200 && response.status !== 404);
  }
  
  sleep(Math.random() * 3 + 1); // 1-4초 랜덤 대기
}
```

### 2. JMeter 테스트 플랜
```xml
<!-- 댓글 작성 시나리오 -->
<HTTPSamplerProxy>
  <elementProp name="HTTPSampler.Arguments">
    <Arguments>
      <collectionProp name="Arguments.arguments">
        <elementProp name="" elementType="HTTPArgument">
          <boolProp name="HTTPArgument.always_encode">false</boolProp>
          <stringProp name="Argument.value">{
            "author": "테스터${__Random(1,1000)}",
            "content": "스트레스 테스트 댓글 ${__time(yyyy-MM-dd HH:mm:ss)}"
          }</stringProp>
          <stringProp name="Argument.metadata">=</stringProp>
        </elementProp>
      </collectionProp>
    </Arguments>
  </elementProp>
  <stringProp name="HTTPSampler.method">POST</stringProp>
  <stringProp name="HTTPSampler.path">/comments/${postId}</stringProp>
</HTTPSamplerProxy>
```

## 🎯 테스트 시나리오별 상세 설정

### 시나리오 A: 일반적인 트래픽 패턴
- **목표**: 평상시 트래픽 시뮬레이션
- **동시 사용자**: 50-100명
- **패턴**: 
  - 게시글 목록 조회 60%
  - 게시글 상세 조회 25%
  - 카테고리 조회 10%
  - 댓글 조회 5%

### 시나리오 B: 피크 트래픽 패턴
- **목표**: 최대 부하 상황 테스트
- **동시 사용자**: 200-500명
- **패턴**: 급격한 트래픽 증가 시뮬레이션

### 시나리오 C: 댓글 폭주 시나리오
- **목표**: 댓글 작성 집중 부하 테스트
- **동시 사용자**: 100명
- **패턴**: 
  - POST /comments 60%
  - GET /comments 30%
  - GET /posts 10%

## 📈 모니터링 지표

### 1. 성능 지표
- **응답 시간**: 평균, 95퍼센타일, 99퍼센타일
- **처리량**: RPS (Requests Per Second)
- **오류율**: HTTP 4xx, 5xx 응답 비율

### 2. 시스템 리소스
- **CPU 사용률**: 70% 이하 유지 목표
- **메모리 사용률**: 80% 이하 유지 목표
- **데이터베이스 연결 풀**: 최대 연결 수 모니터링

### 3. 데이터베이스 성능
- **쿼리 실행 시간**: 각 엔드포인트별 DB 쿼리 성능
- **연결 대기 시간**: 커넥션 풀 상태
- **락 대기**: 데드락 및 락 경합 모니터링

## 🛠️ 테스트 환경 준비

### 1. 테스트 데이터 준비
```sql
-- 게시글 데이터 (최소 100개)
-- 카테고리 데이터 (5-10개)
-- 댓글 데이터 (게시글당 0-20개)
```

### 2. 모니터링 설정
- **APM 도구**: Micrometer + Prometheus + Grafana
- **로그 분석**: ELK Stack 또는 Splunk
- **알림 설정**: 임계값 초과 시 알림

## 🎪 실행 단계

### 1단계: 베이스라인 테스트
```bash
# 단일 사용자 기능 테스트
k6 run --vus 1 --duration 10m baseline-test.js
```

### 2단계: 점진적 부하 증가
```bash
# 점진적 사용자 증가 테스트
k6 run --vus 10 --duration 30m gradual-load-test.js
```

### 3단계: 피크 부하 테스트
```bash
# 최대 부하 테스트
k6 run --vus 500 --duration 10m peak-load-test.js
```

### 4단계: 스파이크 테스트
```bash
# 급격한 트래픽 증가 테스트
k6 run spike-test.js
```

## 🔍 분석 포인트

### 1. 병목 지점 식별
- **데이터베이스 쿼리**: N+1 문제, 인덱스 최적화
- **메모리 누수**: 힙 덤프 분석
- **스레드 경합**: 스레드 덤프 분석

### 2. 최적화 방향
- **캐싱 전략**: Redis 캐시 적용
- **데이터베이스 최적화**: 쿼리 튜닝, 인덱스 추가
- **커넥션 풀 튜닝**: HikariCP 설정 최적화

## 📊 예상 성능 목표

### 최소 성능 기준
- **게시글 목록**: 200ms 이하 (95퍼센타일)
- **게시글 상세**: 150ms 이하 (95퍼센타일)
- **댓글 조회**: 100ms 이하 (95퍼센타일)
- **댓글 작성**: 300ms 이하 (95퍼센타일)
- **카테고리 목록**: 50ms 이하 (95퍼센타일)

### 처리량 목표
- **동시 사용자**: 최소 200명
- **RPS**: 500 requests/second
- **오류율**: 1% 이하 