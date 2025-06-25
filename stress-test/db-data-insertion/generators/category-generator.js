const { faker } = require('@faker-js/faker');

class CategoryGenerator {
  constructor() {
    // 한국어 카테고리 템플릿
    this.categoryTemplates = [
      // 기술 카테고리
      'JavaScript', 'Python', 'Java', 'React', 'Node.js', 'Spring Boot',
      'Vue.js', 'Angular', 'TypeScript', 'Go', 'Rust', 'C++', 'C#',
      'PHP', 'Ruby', 'Swift', 'Kotlin', 'Flutter', 'React Native',
      
      // 개발 주제
      '프론트엔드', '백엔드', '풀스택', 'DevOps', '데이터베이스', 'API',
      '웹개발', '모바일개발', '게임개발', 'AI/ML', '데이터 사이언스',
      '클라우드', 'AWS', 'Azure', 'GCP', 'Docker', 'Kubernetes',
      
      // 일반 주제
      '일상', '여행', '음식', '책리뷰', '영화리뷰', '게임리뷰',
      '취미', '운동', '건강', '요리', '사진', '음악', '예술',
      
      // 비즈니스/커리어
      '스타트업', '창업', '투자', '부동산', '재테크', '커리어',
      '면접후기', '회사생활', '프리랜서', '원격근무', '사이드프로젝트',
      
      // 라이프스타일
      '패션', '뷰티', '인테리어', '가드닝', '펜스타그램', '육아',
      '교육', '자기계발', '독서', '명상', '미니멀라이프'
    ];
  }

  generateCategories(count = 50) {
    const categories = [];
    const usedNames = new Set();
    
    for (let i = 0; i < count; i++) {
      let categoryName;
      
      // 70%는 템플릿에서, 30%는 랜덤 생성
      if (Math.random() < 0.7 && this.categoryTemplates.length > 0) {
        const remainingTemplates = this.categoryTemplates.filter(name => !usedNames.has(name));
        if (remainingTemplates.length > 0) {
          categoryName = faker.helpers.arrayElement(remainingTemplates);
        } else {
          categoryName = this.generateRandomCategoryName();
        }
      } else {
        categoryName = this.generateRandomCategoryName();
      }
      
      // 중복 체크
      let finalName = categoryName;
      let suffix = 1;
      while (usedNames.has(finalName)) {
        finalName = `${categoryName} ${suffix}`;
        suffix++;
      }
      
      usedNames.add(finalName);
      
      categories.push({
        name: finalName,
        ordernum: i + 1,
        created_at: this.generateRandomDate()
      });
    }
    
    // 순서를 랜덤하게 섞기
    return faker.helpers.shuffle(categories).map((category, index) => ({
      ...category,
      ordernum: index + 1
    }));
  }

  generateRandomCategoryName() {
    const types = [
      () => faker.lorem.word() + ' 개발',
      () => faker.company.buzzNoun(),
      () => faker.science.chemicalElement().name,
      () => faker.color.human() + ' ' + faker.animal.type(),
      () => faker.music.genre(),
      () => faker.vehicle.type() + ' 리뷰',
      () => faker.food.adjective() + ' ' + faker.food.dish(),
      () => `${faker.number.int({min: 2020, max: 2024})} ${faker.lorem.word()}`
    ];
    
    return faker.helpers.arrayElement(types)();
  }

  generateRandomDate() {
    // 최근 2년 내의 랜덤한 날짜 생성
    const start = new Date();
    start.setFullYear(start.getFullYear() - 2);
    const end = new Date();
    
    const randomDate = faker.date.between({ from: start, to: end });
    return randomDate.toISOString().slice(0, 19).replace('T', ' ');
  }

  // SQL 삽입을 위한 배열 형태로 변환
  categoriesToSqlValues(categories) {
    return categories.map(category => [
      category.name,
      category.ordernum,
      category.created_at
    ]);
  }
}

module.exports = CategoryGenerator; 