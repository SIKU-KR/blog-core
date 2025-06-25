import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const BASE_URL = "http://localhost:8080";
const errorRate = new Rate("errors");
const spikeTrend = new Trend("spike_response_time");

export let options = {
  stages: [
    { duration: "2m", target: 10 }, // 평상시 트래픽 (10명)
    { duration: "30s", target: 300 }, // 급격한 증가 (300명)
    { duration: "2m", target: 300 }, // 스파이크 유지
    { duration: "30s", target: 10 }, // 급격한 감소
    { duration: "2m", target: 10 }, // 평상시로 복구
  ],
  thresholds: {
    http_req_duration: ["p(95)<1000", "p(99)<2000"],
    http_req_failed: ["rate<0.15"], // 스파이크 테스트에서는 더 관대한 오류율
    errors: ["rate<0.15"],
  },
};

export default function () {
  // 현재 단계 확인 (__ITER는 현재 반복 횟수, __VU는 가상 사용자 ID)
  const currentStage = getCurrentStage();

  // 스파이크 단계에서는 더 빠른 요청 패턴
  if (currentStage === "spike") {
    performSpikeBehavior();
  } else {
    performNormalBehavior();
  }

  // 스파이크 중에는 더 짧은 대기시간
  const waitTime =
    currentStage === "spike"
      ? Math.random() * 2 + 0.5 // 0.5-2.5초
      : Math.random() * 4 + 1; // 1-5초

  sleep(waitTime);
}

function getCurrentStage() {
  // 실행 시간 기반으로 현재 단계 판단
  const elapsedTime = Date.now() - __ENV.START_TIME;
  const minutes = elapsedTime / (1000 * 60);

  if (minutes < 2) return "normal";
  if (minutes < 2.5) return "spike_up";
  if (minutes < 4.5) return "spike";
  if (minutes < 5) return "spike_down";
  return "recovery";
}

function performSpikeBehavior() {
  // 스파이크 시에는 주로 빠른 읽기 작업
  const actions = [
    { weight: 50, action: "getPostsList" },
    { weight: 30, action: "getCategories" },
    { weight: 15, action: "getPostDetail" },
    { weight: 5, action: "getComments" },
  ];

  executeWeightedAction(actions);
}

function performNormalBehavior() {
  // 평상시에는 일반적인 패턴
  const actions = [
    { weight: 40, action: "getPostsList" },
    { weight: 25, action: "getPostDetail" },
    { weight: 20, action: "getComments" },
    { weight: 10, action: "getCategories" },
    { weight: 5, action: "createComment" },
  ];

  executeWeightedAction(actions);
}

function executeWeightedAction(actions) {
  const random = Math.random() * 100;
  let cumulativeWeight = 0;

  for (let action of actions) {
    cumulativeWeight += action.weight;
    if (random <= cumulativeWeight) {
      switch (action.action) {
        case "getPostsList":
          getPostsList();
          break;
        case "getPostDetail":
          getPostDetail();
          break;
        case "getComments":
          getComments();
          break;
        case "getCategories":
          getCategories();
          break;
        case "createComment":
          createComment();
          break;
      }
      break;
    }
  }
}

function getPostsList() {
  const page = Math.floor(Math.random() * 5); // 첫 5페이지에 집중
  const response = http.get(`${BASE_URL}/posts?page=${page}&size=10`);

  const success = check(response, {
    "posts list status is 200": (r) => r.status === 200,
    "posts list response under load": (r) => r.timings.duration < 2000, // 스파이크 시 더 관대한 임계값
  });

  errorRate.add(!success);
  spikeTrend.add(response.timings.duration);
}

function getPostDetail() {
  const postId = Math.floor(Math.random() * 30) + 1; // 인기 게시글에 집중
  const response = http.get(`${BASE_URL}/posts/${postId}`);

  const success = check(response, {
    "post detail status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "post detail response under load": (r) => r.timings.duration < 1500,
  });

  errorRate.add(!success);
  spikeTrend.add(response.timings.duration);
}

function getComments() {
  const postId = Math.floor(Math.random() * 30) + 1;
  const response = http.get(`${BASE_URL}/comments/${postId}`);

  const success = check(response, {
    "comments status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "comments response under load": (r) => r.timings.duration < 1500,
  });

  errorRate.add(!success);
  spikeTrend.add(response.timings.duration);
}

function getCategories() {
  const response = http.get(`${BASE_URL}/categories`);

  const success = check(response, {
    "categories status is 200": (r) => r.status === 200,
    "categories response under load": (r) => r.timings.duration < 1000,
  });

  errorRate.add(!success);
  spikeTrend.add(response.timings.duration);
}

function createComment() {
  const postId = Math.floor(Math.random() * 30) + 1;
  const payload = JSON.stringify({
    author: `스파이크테스터${Math.floor(Math.random() * 1000)}`,
    content: `스파이크 테스트 댓글 ${new Date().toISOString()}`,
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
    },
  };

  const response = http.post(`${BASE_URL}/comments/${postId}`, payload, params);

  const success = check(response, {
    "comment creation under spike load": (r) => r.status === 200 || r.status === 404,
    "comment creation response time": (r) => r.timings.duration < 2000,
  });

  errorRate.add(!success);
  spikeTrend.add(response.timings.duration);
}

export function handleSummary(data) {
  return {
    "spike-test-summary.json": JSON.stringify(data),
    stdout: `
=== 스파이크 테스트 결과 ===
총 요청 수: ${data.metrics.http_reqs.values.count}
평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms
95% 응답 시간: ${data.metrics.http_req_duration.values["p(95)"].toFixed(2)}ms
99% 응답 시간: ${data.metrics.http_req_duration.values["p(99)"].toFixed(2)}ms
최대 응답 시간: ${data.metrics.http_req_duration.values.max.toFixed(2)}ms
오류율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%
최대 처리량: ${data.metrics.http_reqs.values.rate.toFixed(2)} req/s

⚠️  스파이크 테스트에서는 일시적인 성능 저하가 예상됩니다.
중요한 것은 시스템이 복구되는지 확인하는 것입니다.
`,
  };
}
