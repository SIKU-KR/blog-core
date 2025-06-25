import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const BASE_URL = "http://localhost:8080";
const errorRate = new Rate("errors");
const customTrend = new Trend("custom_response_time");

export let options = {
  stages: [
    { duration: "1m", target: 20 }, // 1분간 20명까지 증가
    { duration: "3m", target: 50 }, // 3분간 50명까지 증가
    { duration: "5m", target: 50 }, // 5분간 50명 유지
    { duration: "2m", target: 100 }, // 2분간 100명까지 증가
    { duration: "5m", target: 100 }, // 5분간 100명 유지
    { duration: "2m", target: 0 }, // 2분간 점진적 감소
  ],
  thresholds: {
    http_req_duration: ["p(95)<500", "p(99)<1000"],
    http_req_failed: ["rate<0.05"],
    errors: ["rate<0.05"],
  },
};

export default function () {
  const scenarios = [
    { weight: 70, action: "getPostsList" },
    { weight: 15, action: "getPostDetail" },
    { weight: 10, action: "getCategories" },
    { weight: 5, action: "getComments" },
  ];

  const random = Math.random() * 100;
  let cumulativeWeight = 0;
  let selectedAction = "getPostsList";

  for (let scenario of scenarios) {
    cumulativeWeight += scenario.weight;
    if (random <= cumulativeWeight) {
      selectedAction = scenario.action;
      break;
    }
  }

  switch (selectedAction) {
    case "getPostsList":
      getPostsList();
      break;
    case "getPostDetail":
      getPostDetail();
      break;
    case "getCategories":
      getCategories();
      break;
    case "getComments":
      getComments();
      break;
  }

  // 사용자 행동 패턴을 시뮬레이션 (1-5초 대기)
  sleep(Math.random() * 4 + 1);
}

function getPostsList() {
  const page = Math.floor(Math.random() * 10); // 0-9 페이지
  const size = Math.random() < 0.8 ? 10 : 20; // 80%는 10개, 20%는 20개
  const categoryId = Math.random() < 0.3 ? Math.floor(Math.random() * 5) + 1 : null;

  let url = `${BASE_URL}/posts?page=${page}&size=${size}`;
  if (categoryId) {
    url += `&category=${categoryId}`;
  }

  const response = http.get(url);
  const success = check(response, {
    "posts list status is 200": (r) => r.status === 200,
    "posts list response time < 500ms": (r) => r.timings.duration < 500,
    "posts list has content": (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.success === true && Array.isArray(body.data.content);
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!success);
  customTrend.add(response.timings.duration);
}

function getPostDetail() {
  const postId = Math.floor(Math.random() * 100) + 1; // 1-100 게시글
  const response = http.get(`${BASE_URL}/posts/${postId}`);

  const success = check(response, {
    "post detail status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "post detail response time < 300ms": (r) => r.timings.duration < 300,
    "post detail has valid structure": (r) => {
      if (r.status === 404) return true;
      try {
        const body = JSON.parse(r.body);
        return body.success === true && body.data.id && body.data.title;
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!success);
  customTrend.add(response.timings.duration);
}

function getCategories() {
  const response = http.get(`${BASE_URL}/categories`);

  const success = check(response, {
    "categories status is 200": (r) => r.status === 200,
    "categories response time < 200ms": (r) => r.timings.duration < 200,
    "categories has valid structure": (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.success === true && Array.isArray(body.data);
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!success);
  customTrend.add(response.timings.duration);
}

function getComments() {
  const postId = Math.floor(Math.random() * 100) + 1;
  const response = http.get(`${BASE_URL}/comments/${postId}`);

  const success = check(response, {
    "comments status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "comments response time < 400ms": (r) => r.timings.duration < 400,
    "comments has valid structure": (r) => {
      if (r.status === 404) return true;
      try {
        const body = JSON.parse(r.body);
        return body.success === true && Array.isArray(body.data);
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!success);
  customTrend.add(response.timings.duration);
}

export function handleSummary(data) {
  return {
    "normal-visitor-summary.json": JSON.stringify(data),
    stdout: `
=== 일반 방문자 시나리오 테스트 결과 ===
총 요청 수: ${data.metrics.http_reqs.values.count}
평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms
95% 응답 시간: ${data.metrics.http_req_duration.values["p(95)"].toFixed(2)}ms
99% 응답 시간: ${data.metrics.http_req_duration.values["p(99)"].toFixed(2)}ms
오류율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%
처리량: ${data.metrics.http_reqs.values.rate.toFixed(2)} req/s
`,
  };
}
