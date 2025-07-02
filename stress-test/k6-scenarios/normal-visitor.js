import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const BASE_URL = "http://158.180.93.182:8080";
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
  // 더 현실적인 시나리오 가중치
  const scenarios = [
    { weight: 40, action: "homepageVisitor" },      // 홈페이지 방문자 (가장 일반적)
    { weight: 25, action: "categoryExplorer" },     // 카테고리 탐색자
    { weight: 15, action: "paginationUser" },       // 페이지네이션 사용자
    { weight: 10, action: "commentWriter" },        // 댓글 작성자
    { weight: 10, action: "quickBrowser" },         // 빠른 브라우징
  ];

  const random = Math.random() * 100;
  let cumulativeWeight = 0;
  let selectedAction = "homepageVisitor";

  for (let scenario of scenarios) {
    cumulativeWeight += scenario.weight;
    if (random <= cumulativeWeight) {
      selectedAction = scenario.action;
      break;
    }
  }

  switch (selectedAction) {
    case "homepageVisitor":
      homepageVisitorScenario();
      break;
    case "categoryExplorer":
      categoryExplorerScenario();
      break;
    case "paginationUser":
      paginationUserScenario();
      break;
    case "commentWriter":
      commentWriterScenario();
      break;
    case "quickBrowser":
      quickBrowserScenario();
      break;
  }
}

// 시나리오 1: 홈페이지 방문자 (리스트 + 카테고리 동시 요청 → 게시물 상세 + 댓글 동시 요청)
function homepageVisitorScenario() {
  
  // 1. 홈페이지 로딩: 게시물 리스트와 카테고리를 동시에 요청 (실제 프론트엔드처럼)
  const homePageData = loadHomePage();
  if (!homePageData.postsSuccess) return;
  
  // 페이지 로딩 완료 후 내용을 읽는 시간 (3-8초)
  sleep(Math.random() * 5 + 3);
  
  // 2. 리스트에서 게시물 하나 선택해서 상세보기 (게시물 상세 + 댓글 동시 요청)
  const posts = homePageData.posts;
  if (posts && posts.length > 0) {
    const randomPost = posts[Math.floor(Math.random() * posts.length)];
    const postDetailData = loadPostDetailPage(randomPost.id);
    
    if (postDetailData.postSuccess) {
      // 게시물과 댓글을 읽는 시간 (5-15초)
      sleep(Math.random() * 10 + 5);
    }
  }
}

// 시나리오 2: 카테고리 탐색자 (카테고리 먼저 → 선택한 카테고리 게시물)
function categoryExplorerScenario() {
  
  // 1. 카테고리부터 확인 (카테고리 중심 사용자)
  const categoriesResponse = getCategories();
  if (!categoriesResponse.success) return;
  
  sleep(Math.random() * 2 + 1); // 카테고리 선택 시간
  
  // 2. 특정 카테고리의 게시물 보기 (카테고리별 게시물만 요청)
  const categoryId = Math.floor(Math.random() * 5) + 1;
  const categoryPostsResponse = getPostsList(0, 10, categoryId);
  
  if (categoryPostsResponse.success) {
    sleep(Math.random() * 4 + 2); // 카테고리 게시물 리스트 보는 시간
    
    // 3. 70% 확률로 게시물 상세보기 (게시물 + 댓글 동시 요청)
    const posts = categoryPostsResponse.posts;
    if (Math.random() < 0.7 && posts && posts.length > 0) {
      const randomPost = posts[Math.floor(Math.random() * posts.length)];
      const postDetailData = loadPostDetailPage(randomPost.id);
      
      if (postDetailData.postSuccess) {
        sleep(Math.random() * 8 + 4); // 게시물 읽는 시간
      }
    }
  }
}

// 시나리오 3: 페이지네이션 사용자 (여러 페이지 탐색, 필요시 카테고리도 함께)
function paginationUserScenario() {
  
  const maxPages = Math.floor(Math.random() * 4) + 2; // 2-5 페이지까지 탐색
  
  for (let page = 0; page < maxPages; page++) {
    // 첫 페이지에서는 카테고리도 함께 로딩할 수 있음
    let response;
    if (page === 0 && Math.random() < 0.4) {
      // 40% 확률로 첫 페이지에서 카테고리도 함께 요청
      const homeData = loadHomePage(page, Math.random() < 0.8 ? 10 : 20);
      response = { success: homeData.postsSuccess, posts: homeData.posts };
    } else {
      response = getPostsList(page, Math.random() < 0.8 ? 10 : 20);
    }
    
    if (!response.success) break;
    
    // 페이지 내용 훑어보는 시간
    sleep(Math.random() * 3 + 2);
    
    // 30% 확률로 이 페이지에서 게시물 하나 클릭
    if (Math.random() < 0.3 && response.posts && response.posts.length > 0) {
      const randomPost = response.posts[Math.floor(Math.random() * response.posts.length)];
      const postDetailData = loadPostDetailPage(randomPost.id);
      
      if (postDetailData.postSuccess) {
        sleep(Math.random() * 6 + 3);
        break; // 게시물을 봤으면 페이지네이션 중단
      }
    }
  }
}

// 시나리오 4: 댓글 작성자 (홈페이지 → 게시물 상세 → 댓글 작성)
function commentWriterScenario() {
  
  // 1. 홈페이지에서 시작 (게시물 리스트 + 카테고리 동시 요청)
  const homePageData = loadHomePage();
  if (!homePageData.postsSuccess) return;
  
  sleep(Math.random() * 3 + 2);
  
  // 2. 게시물 상세 보기 (게시물 + 댓글 동시 요청)
  const posts = homePageData.posts;
  if (posts && posts.length > 0) {
    const randomPost = posts[Math.floor(Math.random() * posts.length)];
    const postDetailData = loadPostDetailPage(randomPost.id);
    
    if (postDetailData.postSuccess) {
      // 게시물과 기존 댓글들을 읽는 시간 (댓글 작성자는 더 오래 읽음)
      sleep(Math.random() * 8 + 5);
      
      // 4. 댓글 작성 (60% 확률)
      if (Math.random() < 0.6) {
        postComment(randomPost.id);
        sleep(Math.random() * 2 + 1); // 댓글 작성 후 확인 시간
      }
    }
  }
}

// 시나리오 5: 빠른 브라우징 (여러 게시물을 빠르게 훑어봄)
function quickBrowserScenario() {
  
  // 홈페이지에서 더 많은 게시물 리스트 가져오기
  const homePageData = loadHomePage(0, 20);
  if (!homePageData.postsSuccess) return;
  
  const posts = homePageData.posts;
  if (!posts || posts.length === 0) return;
  
  const postsToView = Math.floor(Math.random() * 4) + 3; // 3-6개 게시물
  
  for (let i = 0; i < postsToView && i < posts.length; i++) {
    const post = posts[i];
    // 빠른 브라우징에서는 게시물만 보고 댓글은 보지 않을 수도 있음
    if (Math.random() < 0.7) {
      // 70% 확률로 게시물 + 댓글 동시 요청
      loadPostDetailPage(post.id);
    } else {
      // 30% 확률로 게시물만 요청
      getPostDetail(post.id);
    }
    sleep(Math.random() * 2 + 1); // 빠르게 훑어보는 시간
  }
}

// 핵심 함수들: 프론트엔드의 실제 동작을 시뮬레이션

// 홈페이지 로딩: 게시물 리스트 + 카테고리 동시 요청
function loadHomePage(page = 0, size = 10) {
  const requests = {
    'posts': {
      method: 'GET',
      url: `${BASE_URL}/posts?page=${page}&size=${size}&sort=createdAt,desc`
    },
    'categories': {
      method: 'GET', 
      url: `${BASE_URL}/categories`
    }
  };

  const responses = http.batch(requests);
  
  // 게시물 리스트 검증
  const postsSuccess = check(responses.posts, {
    "홈페이지 posts status is 200": (r) => r.status === 200,
    "홈페이지 posts response time < 500ms": (r) => r.timings.duration < 500,
    "홈페이지 posts has content": (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.success === true && Array.isArray(body.data.content);
      } catch (e) {
        return false;
      }
    },
  });

  // 카테고리 검증
  const categoriesSuccess = check(responses.categories, {
    "홈페이지 categories status is 200": (r) => r.status === 200,
    "홈페이지 categories response time < 200ms": (r) => r.timings.duration < 200,
  });

  errorRate.add(!postsSuccess);
  errorRate.add(!categoriesSuccess);
  customTrend.add(responses.posts.timings.duration);
  customTrend.add(responses.categories.timings.duration);

  let posts = null;
  if (postsSuccess && responses.posts.status === 200) {
    try {
      const body = JSON.parse(responses.posts.body);
      posts = body.data.content;
         } catch (e) {
       // JSON 파싱 오류 무시
     }
  }

  return { postsSuccess, categoriesSuccess, posts };
}

// 게시물 상세 페이지 로딩: 게시물 상세 + 댓글 동시 요청
function loadPostDetailPage(postId) {
  const requests = {
    'post': {
      method: 'GET',
      url: `${BASE_URL}/posts/${postId}`
    },
    'comments': {
      method: 'GET',
      url: `${BASE_URL}/comments/${postId}`
    }
  };

  const responses = http.batch(requests);

  // 게시물 상세 검증
  const postSuccess = check(responses.post, {
    "게시물 상세 status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "게시물 상세 response time < 300ms": (r) => r.timings.duration < 300,
    "게시물 상세 has valid structure": (r) => {
      if (r.status === 404) return true;
      try {
        const body = JSON.parse(r.body);
        return body.success === true && body.data.id && body.data.title;
      } catch (e) {
        return false;
      }
    },
  });

  // 댓글 검증
  const commentsSuccess = check(responses.comments, {
    "댓글 status is 200 or 404": (r) => r.status === 200 || r.status === 404,
    "댓글 response time < 400ms": (r) => r.timings.duration < 400,
  });

  errorRate.add(!postSuccess);
  errorRate.add(!commentsSuccess);
  customTrend.add(responses.post.timings.duration);
  customTrend.add(responses.comments.timings.duration);

  return { postSuccess, commentsSuccess };
}

// 기존 유틸리티 함수들 (단일 요청용)
function getPostsList(page = 0, size = 10, categoryId = null) {
  let url = `${BASE_URL}/posts?page=${page}&size=${size}&sort=createdAt,desc`;
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

  let posts = null;
  if (success && response.status === 200) {
    try {
      const body = JSON.parse(response.body);
      posts = body.data.content;
         } catch (e) {
       // JSON 파싱 오류 무시
     }
  }

  return { success, posts };
}

function getPostDetail(postId) {
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

  return { success };
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

  return { success };
}

function getComments(postId) {
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

  return { success };
}

function postComment(postId) {
  const commentData = {
    author: `테스터${Math.floor(Math.random() * 1000)}`,
    content: `스트레스 테스트 댓글입니다. ${new Date().toISOString()}`,
    password: "test123!"
  };

  const response = http.post(
    `${BASE_URL}/comments/${postId}`,
    JSON.stringify(commentData),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  const success = check(response, {
    "comment post status is 200 or 400": (r) => r.status === 200 || r.status === 400,
    "comment post response time < 600ms": (r) => r.timings.duration < 600,
  });

  errorRate.add(!success);
  customTrend.add(response.timings.duration);

  return { success };
}

export function handleSummary(data) {
  return {
    "normal-visitor-summary.json": JSON.stringify(data),
    stdout: `
=== 시뮬레이션 테스트 결과 ===
총 요청 수: ${data.metrics.http_reqs.values.count}
평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms
95% 응답 시간: ${data.metrics.http_req_duration.values["p(95)"].toFixed(2)}ms
99% 응답 시간: ${data.metrics.http_req_duration.values["p(99)"].toFixed(2)}ms
오류율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%
처리량: ${data.metrics.http_reqs.values.rate.toFixed(2)} req/s

🏠 홈페이지 방문자: 40% (리스트+카테고리 동시 → 게시물+댓글 동시)
📂 카테고리 탐색자: 25% (카테고리 먼저 → 카테고리별 게시물 → 게시물+댓글 동시)
📄 페이지네이션 사용자: 15% (여러 페이지 탐색, 필요시 카테고리도 함께)
💬 댓글 작성자: 10% (홈페이지 → 게시물+댓글 동시 → 댓글 작성)
⚡ 빠른 브라우징: 10% (여러 게시물 빠르게 훑어봄)

🔄 병렬 요청 패턴:
   - 홈페이지: 게시물 리스트 + 카테고리 동시 요청
   - 게시물 상세: 게시물 + 댓글 동시 요청
`,
  };
}
