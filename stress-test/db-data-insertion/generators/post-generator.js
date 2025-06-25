const { faker } = require('@faker-js/faker');

class PostGenerator {
  constructor() {
    // 한국어 제목 템플릿
    this.titleTemplates = [
      // 기술 관련
      '{}에서 {}를 구현하는 방법',
      '{}와 {}의 차이점 알아보기',
      '{}를 활용한 {} 개발 가이드',
      '초보자를 위한 {} 튜토리얼',
      '{}의 모든 것: 기초부터 고급까지',
      '{}로 {}를 만들어보자',
      '{} 성능 최적화 팁 5가지',
      '{}에서 자주 하는 실수들',
      '2024년 {} 트렌드 분석',
      '{}를 배우기 전에 알아야 할 것들',
      
      // 일반적인 제목
      '{}에 대한 나의 생각',
      '{}를 시작하면서',
      '{}의 장단점 분석',
      '{}를 사용해본 후기',
      '{}와 관련된 흥미로운 사실들',
      '{}를 추천하는 이유',
      '{}에서 배운 교훈들',
      '{}의 미래 전망',
      '{}를 선택한 이유',
      '{}에 관한 완벽 가이드'
    ];

    // 기술 키워드
    this.techKeywords = [
      'React', 'Vue.js', 'Angular', 'JavaScript', 'TypeScript', 'Node.js',
      'Spring Boot', 'Django', 'Flask', 'Express.js', 'Next.js', 'Nuxt.js',
      'Docker', 'Kubernetes', 'AWS', 'Azure', 'GCP', 'MySQL', 'PostgreSQL',
      'MongoDB', 'Redis', 'GraphQL', 'REST API', 'Microservices', 'DevOps',
      'CI/CD', 'Git', 'GitHub', 'GitLab', 'Jenkins', 'Terraform'
    ];

    // 일반 키워드
    this.generalKeywords = [
      '여행', '음식', '영화', '책', '운동', '건강', '요리', '사진',
      '음악', '게임', '취미', '투자', '부동산', '창업', '커리어',
      '자기계발', '독서', '명상', '미니멀라이프', '패션', '뷰티'
    ];

    this.postStates = ['PUBLISHED', 'DRAFT', 'ARCHIVED'];
  }

  generatePosts(count = 500, categoryIds = []) {
    if (categoryIds.length === 0) {
      throw new Error('카테고리 ID 배열이 필요합니다.');
    }

    const posts = [];
    
    for (let i = 0; i < count; i++) {
      const categoryId = faker.helpers.arrayElement(categoryIds);
      const title = this.generateTitle();
      const content = this.generateContent(title);
      const summary = this.generateSummary(content);
      const state = this.getWeightedState();
      const createdAt = this.generateRandomDate();
      const updatedAt = this.generateUpdatedDate(createdAt);

      posts.push({
        title,
        content,
        summary,
        state,
        category_id: categoryId,
        created_at: createdAt,
        updated_at: updatedAt
      });
    }

    return posts;
  }

  generateTitle() {
    const template = faker.helpers.arrayElement(this.titleTemplates);
    
    if (template.includes('{}')) {
      const placeholderCount = (template.match(/{}/g) || []).length;
      let title = template;
      
      for (let i = 0; i < placeholderCount; i++) {
        const keyword = Math.random() < 0.6 
          ? faker.helpers.arrayElement(this.techKeywords)
          : faker.helpers.arrayElement(this.generalKeywords);
        title = title.replace('{}', keyword);
      }
      
      return title;
    }
    
    // 템플릿에 placeholder가 없는 경우
    return template;
  }

  generateContent(title) {
    const sections = [];
    
    // 서론
    sections.push(this.generateIntroduction(title));
    
    // 본문 (2-5개 섹션)
    const sectionCount = faker.number.int({ min: 2, max: 5 });
    for (let i = 0; i < sectionCount; i++) {
      sections.push(this.generateSection());
    }
    
    // 결론
    sections.push(this.generateConclusion());
    
    return sections.join('\n\n');
  }

  generateIntroduction(title) {
    const intros = [
      `${title}에 대해 알아보겠습니다.`,
      `오늘은 ${title.split(' ')[0]}에 관한 이야기를 해보려 합니다.`,
      `최근에 ${title.split(' ')[0]}에 대해 많은 관심이 생겨서 정리해보았습니다.`,
      `${title}는 많은 분들이 궁금해하시는 주제인 것 같습니다.`
    ];
    
    const intro = faker.helpers.arrayElement(intros);
    const explanation = faker.lorem.paragraphs(faker.number.int({ min: 1, max: 3 }));
    
    return `${intro}\n\n${explanation}`;
  }

  generateSection() {
    const sectionTitle = `## ${faker.lorem.words(faker.number.int({ min: 2, max: 5 }))}`;
    const content = faker.lorem.paragraphs(faker.number.int({ min: 2, max: 4 }));
    
    // 30% 확률로 코드 블록 추가
    if (Math.random() < 0.3) {
      const codeBlock = this.generateCodeBlock();
      return `${sectionTitle}\n\n${content}\n\n${codeBlock}`;
    }
    
    // 20% 확률로 리스트 추가
    if (Math.random() < 0.2) {
      const list = this.generateList();
      return `${sectionTitle}\n\n${content}\n\n${list}`;
    }
    
    return `${sectionTitle}\n\n${content}`;
  }

  generateCodeBlock() {
    const languages = ['javascript', 'python', 'java', 'sql', 'bash', 'json'];
    const language = faker.helpers.arrayElement(languages);
    const codeLines = [];
    
    for (let i = 0; i < faker.number.int({ min: 3, max: 8 }); i++) {
      codeLines.push(faker.lorem.words(faker.number.int({ min: 3, max: 8 })));
    }
    
    return `\`\`\`${language}\n${codeLines.join('\n')}\n\`\`\``;
  }

  generateList() {
    const listItems = [];
    const itemCount = faker.number.int({ min: 3, max: 7 });
    
    for (let i = 0; i < itemCount; i++) {
      listItems.push(`- ${faker.lorem.sentence()}`);
    }
    
    return listItems.join('\n');
  }

  generateConclusion() {
    const conclusions = [
      '이상으로 정리를 마치겠습니다.',
      '도움이 되셨기를 바랍니다.',
      '궁금한 점이 있으시면 댓글로 남겨주세요.',
      '다음에도 유익한 내용으로 찾아뵙겠습니다.',
      '읽어주셔서 감사합니다.'
    ];
    
    const conclusion = faker.helpers.arrayElement(conclusions);
    const additionalContent = faker.lorem.paragraph();
    
    return `${additionalContent}\n\n${conclusion}`;
  }

  generateSummary(content) {
    // 내용의 첫 번째 문단을 요약으로 사용 (150자 제한)
    const firstParagraph = content.split('\n\n')[1] || content.split('\n\n')[0];
    const cleanText = firstParagraph.replace(/#+\s/g, '').replace(/`/g, '');
    
    if (cleanText.length <= 150) {
      return cleanText;
    }
    
    return cleanText.substring(0, 147) + '...';
  }

  getWeightedState() {
    return 'PUBLISHED';
  }

  generateRandomDate() {
    // 최근 1년 내의 랜덤한 날짜 생성
    const start = new Date();
    start.setFullYear(start.getFullYear() - 1);
    const end = new Date();
    
    const randomDate = faker.date.between({ from: start, to: end });
    return randomDate.toISOString().slice(0, 19).replace('T', ' ');
  }

  generateUpdatedDate(createdAt) {
    const created = new Date(createdAt);
    
    // 70% 확률로 생성일과 동일, 30% 확률로 이후 날짜
    if (Math.random() < 0.7) {
      return createdAt;
    }
    
    const end = new Date();
    const updatedDate = faker.date.between({ from: created, to: end });
    return updatedDate.toISOString().slice(0, 19).replace('T', ' ');
  }

  // SQL 삽입을 위한 배열 형태로 변환
  postsToSqlValues(posts) {
    return posts.map(post => [
      post.title,
      post.content,
      post.summary,
      post.state,
      post.category_id,
      post.created_at,
      post.updated_at
    ]);
  }
}

module.exports = PostGenerator; 