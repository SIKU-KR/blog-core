import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Counter } from "k6/metrics";

const BASE_URL = "http://localhost:8080";
const errorRate = new Rate("errors");
const commentCreated = new Counter("comments_created");
const commentsFetched = new Counter("comments_fetched");

export let options = {
  stages: [
    { duration: "1m", target: 30 }, // 1분간 30명까지 증가
    { duration: "5m", target: 50 }, // 5분간 50명까지 증가
    { duration: "10m", target: 100 }, // 10분간 100명까지 증가
    { duration: "5m", target: 100 }, // 5분간 100명 유지
    { duration: "2m", target: 0 }, // 2분간 점진적 감소
  ],
  thresholds: {
    http_req_duration: ["p(95)<800", "p(99)<1500"],
    http_req_failed: ["rate<0.1"],
    errors: ["rate<0.1"],
  },
};

// 댓글 작성용 샘플 데이터
const sampleAuthors = ["김철수", "이영희", "박민수", "최정희", "장동건", "송혜교", "유재석", "강호동", "이효리", "김태희", "현빈", "전지현", "원빈", "김희선", "조인성", "한가인"];

const sampleComments = [
  "정말 유익한 글이네요!",
  "공감합니다. 좋은 정보 감사해요.",
  "저도 비슷한 경험이 있어서 더욱 와닿네요.",
  "다음 포스팅도 기대하고 있겠습니다.",
  "질문이 있는데, 혹시 더 자세한 설명 부탁드려요.",
  "이런 관점은 생각해보지 못했네요. 새롭습니다!",
  "실용적인 팁들이 많아서 도움이 됩니다.",
  "저도 이 방법을 써봐야겠어요.",
  "흥미로운 내용이네요. 더 알고 싶습니다.",
  "명확한 설명 감사합니다!",
];

export default function () {
  const scenarios = [
    { weight: 60, action: "createComment" },
    { weight: 30, action: "getComments" },
    { weight: 10, action: "getPostDetail" },
  ];

  const random = Math.random() * 100;
  let cumulativeWeight = 0;
  let selectedAction = "createComment";

  for (let scenario of scenarios) {
    cumulativeWeight += scenario.weight;
    if (random <= cumulativeWeight) {
      selectedAction = scenario.action;
      break;
    }
  }

  switch (selectedAction) {
    case "createComment":
      createComment();
      break;
    case "getComments":
      getComments();
      break;
    case "getPostDetail":
      getPostDetail();
      break;
  }

  // 댓글 작성 후 잠깐 대기 (2-8초)
  sleep(Math.random() * 6 + 2);
}

function createComment() {
  const postId = Math.floor(Math.random() * 50) + 1; // 1-50 게시글
  const author = sampleAuthors[Math.floor(Math.random() * sampleAuthors.length)];
  const content = sampleComments[Math.floor(Math.random() * sampleComments.length)];

  const payload = JSON.stringify({
    author: author,
    content: content,
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
    },
  };

  const response = http.post(`${BASE_URL}/comments/${postId}`, payload, params);

  const success = check(response, {
    "comment creation status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "comment creation response time < 800ms": (r) => r.timings.duration < 800,
    "comment creation returns valid response": (r) => {
      if (r.status === 404) return true; // 게시글이 없는 경우
      try {
        const body = JSON.parse(r.body);
        return body.success === true && body.data.id;
      } catch (e) {
        return false;
      }
    },
  });

  if (response.status === 200) {
    commentCreated.add(1);
  }

  errorRate.add(!success);
}

function getComments() {
  const postId = Math.floor(Math.random() * 50) + 1;
  const response = http.get(`${BASE_URL}/comments/${postId}`);

  const success = check(response, {
    "comments fetch status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "comments fetch response time < 400ms": (r) => r.timings.duration < 400,
    "comments fetch returns valid structure": (r) => {
      if (r.status === 404) return true;
      try {
        const body = JSON.parse(r.body);
        return body.success === true && Array.isArray(body.data);
      } catch (e) {
        return false;
      }
    },
  });

  if (response.status === 200) {
    commentsFetched.add(1);
  }

  errorRate.add(!success);
}

function getPostDetail() {
  const postId = Math.floor(Math.random() * 50) + 1;
  const response = http.get(`${BASE_URL}/posts/${postId}`);

  const success = check(response, {
    "post detail status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "post detail response time < 300ms": (r) => r.timings.duration < 300,
  });

  errorRate.add(!success);
}

export function handleSummary(data) {
  return {
    "comment-heavy-summary.json": JSON.stringify(data),
    stdout: `
=== 댓글 중심 부하 테스트 결과 ===
총 요청 수: ${data.metrics.http_reqs.values.count}
댓글 생성 수: ${data.metrics.comments_created ? data.metrics.comments_created.values.count : 0}
댓글 조회 수: ${data.metrics.comments_fetched ? data.metrics.comments_fetched.values.count : 0}
평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms
95% 응답 시간: ${data.metrics.http_req_duration.values["p(95)"].toFixed(2)}ms
99% 응답 시간: ${data.metrics.http_req_duration.values["p(99)"].toFixed(2)}ms
오류율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%
처리량: ${data.metrics.http_reqs.values.rate.toFixed(2)} req/s
`,
  };
}
