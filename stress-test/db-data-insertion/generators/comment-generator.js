const { faker } = require('@faker-js/faker');

class CommentGenerator {
  constructor() {
    // 한국 이름 템플릿
    this.koreanLastNames = [
      '김', '이', '박', '최', '정', '강', '조', '윤', '장', '임',
      '한', '오', '서', '신', '권', '황', '안', '송', '전', '홍',
      '유', '고', '문', '양', '손', '배', '조', '백', '허', '남'
    ];

    this.koreanFirstNames = [
      '민수', '영희', '철수', '순이', '현우', '지영', '태현', '수빈',
      '도윤', '서윤', '예준', '하은', '시우', '하린', '준서', '지우',
      '건우', '서현', '민준', '서연', '유준', '채원', '도현', '지민',
      '현서', '다은', '지후', '소율', '수호', '예린', '주원', '가은'
    ];

    // 댓글 템플릿
    this.commentTemplates = [
      // 긍정적 반응
      '정말 유익한 글이네요! 감사합니다.',
      '좋은 정보 공유해주셔서 고맙습니다.',
      '이런 내용을 찾고 있었는데 도움이 많이 됐어요.',
      '명확한 설명 덕분에 이해가 잘 됐습니다.',
      '실무에 바로 적용할 수 있을 것 같아요.',
      '다음 포스팅도 기대하고 있겠습니다.',
      '저도 비슷한 경험이 있어서 더욱 공감됩니다.',
      '이 방법 정말 좋은 것 같아요!',
      '새로운 관점을 알게 되어 좋았습니다.',
      '상세한 설명 감사드려요.',

      // 질문형
      '혹시 {}에 대해서도 설명해주실 수 있나요?',
      '{}는 어떻게 해결하셨나요?',
      '이 방법 말고 다른 방법도 있을까요?',
      '초보자도 따라할 수 있는 방법인가요?',
      '실제로 사용해보신 결과는 어떠셨나요?',
      '성능상 차이가 많이 나나요?',
      '관련 자료가 더 있다면 공유 부탁드려요.',
      '이 내용을 더 자세히 알고 싶은데 추천 자료가 있나요?',

      // 경험 공유형
      '저는 {}로 해결했었는데, 이 방법도 좋네요.',
      '회사에서 {}를 사용하고 있는데 참고하겠습니다.',
      '예전에 비슷한 문제로 고생했던 기억이 나네요.',
      '저도 최근에 {}를 공부하고 있어서 도움이 됐어요.',
      '실무에서 {}를 써봤는데 정말 편리했습니다.',

      // 간단한 반응
      '👍 좋아요!',
      '잘 봤습니다!',
      '감사해요~',
      '굿굿',
      '완전 유용하네요!',
      '최고입니다 👏',
      '대박!',
      '멋져요!',
      '참고할게요!',
      '북마크 했어요!'
    ];

    // 기술 키워드 (질문이나 경험 공유에 사용)
    this.techKeywords = [
      'React', 'Vue', 'JavaScript', 'TypeScript', 'Node.js', 'Python',
      'Java', 'Spring', 'MySQL', 'MongoDB', 'Redis', 'Docker',
      'AWS', 'API', 'Git', 'VS Code', 'Linux', 'Ubuntu'
    ];
  }

  generateComments(count = 2000, postIds = []) {
    if (postIds.length === 0) {
      throw new Error('게시글 ID 배열이 필요합니다.');
    }

    const comments = [];
    
    for (let i = 0; i < count; i++) {
      const postId = this.selectPostIdByWeight(postIds);
      const authorName = this.generateKoreanName();
      const content = this.generateCommentContent();
      const createdAt = this.generateRandomDate();

      comments.push({
        post_id: postId,
        author_name: authorName,
        content,
        created_at: createdAt
      });
    }

    // 게시글별로 정렬 (성능 최적화)
    return comments.sort((a, b) => a.post_id - b.post_id);
  }

  selectPostIdByWeight(postIds) {
    // 80-20 법칙 적용: 20%의 인기 게시글에 80%의 댓글
    const random = Math.random();
    
    if (random < 0.8) {
      // 상위 20% 게시글 (최근 게시글들이 더 인기있다고 가정)
      const topPosts = postIds.slice(-Math.ceil(postIds.length * 0.2));
      return faker.helpers.arrayElement(topPosts);
    } else {
      // 나머지 80% 게시글
      const regularPosts = postIds.slice(0, -Math.ceil(postIds.length * 0.2));
      return faker.helpers.arrayElement(regularPosts.length > 0 ? regularPosts : postIds);
    }
  }

  generateKoreanName() {
    const lastName = faker.helpers.arrayElement(this.koreanLastNames);
    const firstName = faker.helpers.arrayElement(this.koreanFirstNames);
    
    // 10% 확률로 영어 이름
    if (Math.random() < 0.1) {
      return faker.person.firstName() + faker.number.int({ min: 1, max: 999 });
    }
    
    // 20% 확률로 닉네임 형태
    if (Math.random() < 0.2) {
      const nicknames = [
        `${firstName}_${faker.number.int({ min: 10, max: 99 })}`,
        `${lastName}${firstName}`,
        `dev_${firstName}`,
        `${firstName}_coder`,
        `${lastName}_${faker.number.int({ min: 1, max: 999 })}`
      ];
      return faker.helpers.arrayElement(nicknames);
    }
    
    return `${lastName}${firstName}`;
  }

  generateCommentContent() {
    const template = faker.helpers.arrayElement(this.commentTemplates);
    
    // 템플릿에 placeholder가 있는 경우 키워드로 대체
    if (template.includes('{}')) {
      const keyword = faker.helpers.arrayElement(this.techKeywords);
      return template.replace('{}', keyword);
    }
    
    // 30% 확률로 추가 내용 붙이기
    if (Math.random() < 0.3) {
      const additionalContent = this.generateAdditionalContent();
      return `${template} ${additionalContent}`;
    }
    
    return template;
  }

  generateAdditionalContent() {
    const additionalTypes = [
      '다음에도 좋은 글 부탁드려요!',
      '많은 도움이 되었습니다.',
      '블로그 구독하고 갑니다.',
      '공유도 하고 싶네요.',
      '동료들에게도 추천해야겠어요.',
      '실무에서 써보고 후기 남길게요.',
      '이런 시리즈로 계속 써주시면 좋겠어요.',
      '처음 알게 된 내용이네요.',
      '공부가 많이 됐습니다.',
      '책갈피 해두고 나중에 다시 봐야겠어요.'
    ];
    
    return faker.helpers.arrayElement(additionalTypes);
  }

  generateRandomDate() {
    // 최근 6개월 내의 랜덤한 날짜 생성
    const start = new Date();
    start.setMonth(start.getMonth() - 6);
    const end = new Date();
    
    const randomDate = faker.date.between({ from: start, to: end });
    return randomDate.toISOString().slice(0, 19).replace('T', ' ');
  }

  // 게시글별 댓글 분포 생성 (일부 게시글에 댓글이 집중되도록)
  generateCommentsDistribution(totalComments, postIds) {
    const distribution = new Map();
    
    // 모든 게시글을 초기화
    postIds.forEach(id => distribution.set(id, 0));
    
    // 80-20 법칙 적용
    const popularPostCount = Math.ceil(postIds.length * 0.2);
    const popularPosts = faker.helpers.shuffle(postIds).slice(0, popularPostCount);
    const regularPosts = postIds.filter(id => !popularPosts.includes(id));
    
    // 80%의 댓글을 20%의 인기 게시글에 배분
    const popularComments = Math.floor(totalComments * 0.8);
    const regularComments = totalComments - popularComments;
    
    // 인기 게시글에 댓글 배분
    for (let i = 0; i < popularComments; i++) {
      const postId = faker.helpers.arrayElement(popularPosts);
      distribution.set(postId, distribution.get(postId) + 1);
    }
    
    // 일반 게시글에 댓글 배분
    for (let i = 0; i < regularComments; i++) {
      const postId = faker.helpers.arrayElement(regularPosts);
      distribution.set(postId, distribution.get(postId) + 1);
    }
    
    return distribution;
  }

  // SQL 삽입을 위한 배열 형태로 변환
  commentsToSqlValues(comments) {
    return comments.map(comment => [
      comment.post_id,
      comment.author_name,
      comment.content,
      comment.created_at
    ]);
  }
}

module.exports = CommentGenerator; 