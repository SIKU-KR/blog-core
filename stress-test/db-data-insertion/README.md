# 🎲 블로그 데이터 시딩 도구

faker.js를 이용한 대량 모킹 데이터 생성 및 삽입 도구입니다. 스트레스 테스트를 위한 현실적인 데이터를 자동으로 생성합니다.

## 📊 생성되는 데이터

| 항목 | 개수 | 특징 |
|------|------|------|
| **카테고리** | 50개 이상 | 기술/개발, 일상, 비즈니스 등 다양한 주제 |
| **게시글** | 500개 이상 | 마크다운 형식, 코드블록, 리스트 포함 |
| **댓글** | 2000개 이상 | 80-20 법칙 적용 (인기 게시글에 댓글 집중) |

## 🚀 빠른 시작

### 1. 자동 설치 및 실행

```bash
cd stress-test/db-data-insertion
chmod +x install-and-run.sh
./install-and-run.sh
```

### 2. 단계별 실행

```bash
# 1. 의존성 설치
npm install

# 2. 환경 설정 (.env 파일 수정)
cp .env.example .env
nano .env

# 3. 데이터 시딩 실행
node seed-data.js
```

## ⚙️ 환경 설정

`.env` 파일을 수정하여 설정을 변경할 수 있습니다:

```env
# 데이터베이스 연결
DB_HOST=localhost
DB_PORT=3306
DB_NAME=blog_db
DB_USER=root
DB_PASSWORD=password

# 생성할 데이터 수량
CATEGORIES_COUNT=50
POSTS_COUNT=500
COMMENTS_COUNT=2000

# 성능 설정
BATCH_SIZE=100
```

## 🎯 데이터 특징

### 카테고리 (50개+)
- **기술 카테고리**: JavaScript, Python, React, Spring Boot 등
- **개발 주제**: 프론트엔드, 백엔드, DevOps, 데이터베이스 등  
- **일반 주제**: 여행, 음식, 책리뷰, 영화리뷰 등
- **비즈니스**: 스타트업, 투자, 커리어, 창업 등

### 게시글 (500개+)
- **제목**: 현실적인 한국어 템플릿 기반
- **내용**: 마크다운 형식으로 구조화
- **요소**: 코드블록(30%), 리스트(20%) 자동 포함
- **상태**: PUBLISHED(85%), DRAFT(10%), ARCHIVED(5%)
- **날짜**: 최근 1년 내 랜덤 분포

### 댓글 (2000개+)
- **작성자**: 한국 이름 + 닉네임 혼합
- **내용**: 다양한 반응 타입 (긍정, 질문, 경험공유)
- **분포**: 80-20 법칙 (20% 인기 게시글에 80% 댓글)
- **날짜**: 최근 6개월 내 분포

## 🛠️ 사용 가능한 명령어

```bash
# 전체 과정 실행
./install-and-run.sh

# 개별 작업
./install-and-run.sh install    # 의존성만 설치
./install-and-run.sh seed       # 데이터 시딩만 실행
./install-and-run.sh clean      # 모든 데이터 삭제
./install-and-run.sh test       # DB 연결 테스트

# npm 스크립트
npm run seed                    # 전체 시딩
npm run seed:categories         # 카테고리만
npm run seed:posts             # 게시글만  
npm run seed:comments          # 댓글만
npm run clean                  # 데이터 정리
```

## 📈 성능 최적화

### 배치 처리
- 기본 배치 크기: 100개
- 환경변수 `BATCH_SIZE`로 조정 가능
- 메모리 사용량과 속도의 균형

### 진행 상황 표시
- 실시간 진행률 표시
- 단계별 상태 정보
- 색상으로 구분된 메시지

### 메모리 최적화
- 스트림 방식 데이터 처리
- 배치 단위 메모리 해제
- 대용량 데이터 처리 가능

## 📊 실행 결과 예시

```
🚀 블로그 데이터 시딩 시작
📊 생성할 데이터: 카테고리 50개, 게시글 500개, 댓글 2000개

📁 카테고리 생성 중...
████████████████████████████████████████ | 100% | 50/50 | 카테고리 삽입
✅ 카테고리 50개 생성 완료

📝 게시글 생성 중...
████████████████████████████████████████ | 100% | 500/500 | 게시글 삽입  
✅ 게시글 500개 생성 완료

💬 댓글 생성 중...
████████████████████████████████████████ | 100% | 2000/2000 | 댓글 삽입
✅ 댓글 2000개 생성 완료

🎉 데이터 시딩 완료!
==================================================
📁 카테고리: 50개
📝 게시글: 500개  
💬 댓글: 2,000개
==================================================

📊 게시글 상태별 통계:
  PUBLISHED: 425개
  DRAFT: 50개
  ARCHIVED: 25개

📈 카테고리별 게시글 수 (상위 10개):
  JavaScript: 23개
  React: 19개
  Spring Boot: 18개
  ...

🔥 댓글이 많은 게시글 (상위 5개):
  React에서 TypeScript를 구현하는 방법: 45개
  초보자를 위한 JavaScript 튜토리얼: 38개
  ...
```

## 🔧 커스터마이징

### 데이터 생성 규칙 수정

```javascript
// generators/category-generator.js
this.categoryTemplates = [
  '커스텀 카테고리1',
  '커스텀 카테고리2',
  // ... 추가 카테고리
];

// generators/post-generator.js  
this.titleTemplates = [
  '{}에 대한 새로운 접근법',
  // ... 추가 제목 템플릿
];

// generators/comment-generator.js
this.commentTemplates = [
  '새로운 댓글 템플릿',
  // ... 추가 댓글 템플릿  
];
```

### 데이터 분포 조정

```javascript
// 게시글 상태 분포 (post-generator.js)
getWeightedState() {
  const random = Math.random();
  if (random < 0.90) return 'PUBLISHED';  // 90%로 증가
  if (random < 0.95) return 'DRAFT';      // 5%로 감소
  return 'ARCHIVED';                      // 5% 유지
}

// 댓글 분포 (comment-generator.js)
selectPostIdByWeight(postIds) {
  const random = Math.random();
  if (random < 0.9) {  // 90%로 증가
    // 인기 게시글 선택
  }
}
```

## 🚨 주의사항

1. **데이터베이스 백업**: 기존 데이터가 삭제됩니다
2. **메모리 사용량**: 대량 데이터 생성 시 메모리 확인
3. **외래키 제약**: 테이블 삭제 순서 준수 필요
4. **인덱스 최적화**: 대량 삽입 후 인덱스 재구성 권장

## 🔍 트러블슈팅

### 데이터베이스 연결 실패
```bash
# 연결 정보 확인
cat .env | grep DB_

# 연결 테스트
./install-and-run.sh test
```

### 메모리 부족
```bash
# 배치 크기 조정
echo "BATCH_SIZE=50" >> .env

# Node.js 힙 크기 증가
export NODE_OPTIONS="--max-old-space-size=4096"
```

### 외래키 제약 조건 오류
```sql
-- 외래키 제약 조건 확인
SHOW CREATE TABLE posts;
SHOW CREATE TABLE comments;

-- 수동 정리
DELETE FROM comments;
DELETE FROM posts;  
DELETE FROM categories;
```

## 📚 기술 스택

- **Node.js**: 런타임 환경
- **@faker-js/faker**: 모킹 데이터 생성
- **mysql2**: MySQL 데이터베이스 연결
- **cli-progress**: 진행률 표시
- **colors**: 터미널 색상
- **dotenv**: 환경변수 관리


**💡 팁**: 스트레스 테스트 전에 반드시 충분한 데이터를 생성하여 현실적인 테스트 환경을 구성하세요!
