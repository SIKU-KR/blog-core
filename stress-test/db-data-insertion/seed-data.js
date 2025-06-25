#!/usr/bin/env node

const Database = require('./config/database');
const CategoryGenerator = require('./generators/category-generator');
const PostGenerator = require('./generators/post-generator');
const CommentGenerator = require('./generators/comment-generator');
const cliProgress = require('cli-progress');
const colors = require('colors');

require('dotenv').config();

class DataSeeder {
  constructor() {
    this.db = new Database();
    this.categoryGen = new CategoryGenerator();
    this.postGen = new PostGenerator();
    this.commentGen = new CommentGenerator();
    
    // 설정값
    this.config = {
      categories: parseInt(process.env.CATEGORIES_COUNT) || 50,
      posts: parseInt(process.env.POSTS_COUNT) || 500,
      comments: parseInt(process.env.COMMENTS_COUNT) || 2000,
      batchSize: parseInt(process.env.BATCH_SIZE) || 100
    };
    
    // 진행 상황 표시
    this.progressBar = new cliProgress.SingleBar({
      format: colors.cyan('{bar}') + ' | {percentage}% | {value}/{total} | {stage}',
      barCompleteChar: '\u2588',
      barIncompleteChar: '\u2591',
      hideCursor: true
    });
  }

  async run() {
    try {
      console.log(colors.blue('🚀 블로그 데이터 시딩 시작'));
      console.log(colors.yellow(`📊 생성할 데이터: 카테고리 ${this.config.categories}개, 게시글 ${this.config.posts}개, 댓글 ${this.config.comments}개`));
      
      // 데이터베이스 연결
      await this.db.connect();
      
      // 기존 데이터 확인 및 정리
      await this.checkAndCleanDatabase();
      
      // 데이터 생성 및 삽입
      const categoryIds = await this.seedCategories();
      const postIds = await this.seedPosts(categoryIds);
      await this.seedComments(postIds);
      
      // 완료 메시지
      await this.showCompletionSummary();
      
    } catch (error) {
      console.error(colors.red('❌ 시딩 과정에서 오류 발생:'), error.message);
      process.exit(1);
    } finally {
      await this.db.disconnect();
    }
  }

  async checkAndCleanDatabase() {
    console.log(colors.yellow('\n🔍 기존 데이터 확인 중...'));
    
    const commentCount = await this.db.getTableCount('comments');
    const postCount = await this.db.getTableCount('posts');
    const categoryCount = await this.db.getTableCount('categories');
    
    if (commentCount > 0 || postCount > 0 || categoryCount > 0) {
      console.log(colors.yellow(`기존 데이터 발견: 카테고리 ${categoryCount}개, 게시글 ${postCount}개, 댓글 ${commentCount}개`));
      
      // 사용자 확인 (CI 환경에서는 자동으로 Yes)
      if (!process.env.CI) {
        const readline = require('readline').createInterface({
          input: process.stdin,
          output: process.stdout
        });
        
        const answer = await new Promise(resolve => {
          readline.question('기존 데이터를 삭제하고 새로 생성하시겠습니까? (y/N): ', resolve);
        });
        
        readline.close();
        
        if (answer.toLowerCase() !== 'y') {
          console.log(colors.yellow('시딩을 취소합니다.'));
          process.exit(0);
        }
      }
      
      // 외래키 제약 조건 때문에 역순으로 삭제
      console.log(colors.yellow('🧹 기존 데이터 삭제 중...'));
      await this.db.clearTable('comments');
      await this.db.clearTable('posts');
      await this.db.clearTable('categories');
    }
  }

  async seedCategories() {
    console.log(colors.green('\n📁 카테고리 생성 중...'));
    
    // 카테고리 데이터 생성
    const categories = this.categoryGen.generateCategories(this.config.categories);
    const sqlValues = this.categoryGen.categoriesToSqlValues(categories);
    
    // 배치로 삽입
    this.progressBar.start(this.config.categories, 0, { stage: '카테고리 삽입' });
    
    const query = 'INSERT INTO categories (name, order_num, created_at) VALUES (:1, :2, TO_TIMESTAMP(:3, \'YYYY-MM-DD HH24:MI:SS\'))';
    await this.db.executeBatch(query, sqlValues);
    
    this.progressBar.update(this.config.categories);
    this.progressBar.stop();
    
    // 생성된 카테고리 ID 조회
    const result = await this.db.execute('SELECT id FROM categories ORDER BY id');
    const categoryIds = result.rows.map(row => row.ID);
    
    console.log(colors.green(`✅ 카테고리 ${categoryIds.length}개 생성 완료`));
    return categoryIds;
  }

  async seedPosts(categoryIds) {
    console.log(colors.green('\n📝 게시글 생성 중...'));
    
    // 게시글 데이터 생성
    const posts = this.postGen.generatePosts(this.config.posts, categoryIds);
    const sqlValues = this.postGen.postsToSqlValues(posts);
    
    // 배치 단위로 삽입
    this.progressBar.start(this.config.posts, 0, { stage: '게시글 삽입' });
    
    const query = 'INSERT INTO posts (title, content, summary, state, category_id, created_at, updated_at) VALUES (:1, :2, :3, :4, :5, TO_TIMESTAMP(:6, \'YYYY-MM-DD HH24:MI:SS\'), TO_TIMESTAMP(:7, \'YYYY-MM-DD HH24:MI:SS\'))';
    
    for (let i = 0; i < sqlValues.length; i += this.config.batchSize) {
      const batch = sqlValues.slice(i, i + this.config.batchSize);
      await this.db.executeBatch(query, batch);
      this.progressBar.update(Math.min(i + this.config.batchSize, this.config.posts));
    }
    
    this.progressBar.stop();
    
    // 생성된 게시글 ID 조회
    const result = await this.db.execute('SELECT id FROM posts ORDER BY id');
    const postIds = result.rows.map(row => row.ID);
    
    console.log(colors.green(`✅ 게시글 ${postIds.length}개 생성 완료`));
    return postIds;
  }

  async seedComments(postIds) {
    console.log(colors.green('\n💬 댓글 생성 중...'));
    
    // 댓글 데이터 생성
    const comments = this.commentGen.generateComments(this.config.comments, postIds);
    const sqlValues = this.commentGen.commentsToSqlValues(comments);
    
    // 배치 단위로 삽입
    this.progressBar.start(this.config.comments, 0, { stage: '댓글 삽입' });
    
    const query = 'INSERT INTO comments (post_id, author_name, content, created_at) VALUES (:1, :2, :3, TO_TIMESTAMP(:4, \'YYYY-MM-DD HH24:MI:SS\'))';
    
    for (let i = 0; i < sqlValues.length; i += this.config.batchSize) {
      const batch = sqlValues.slice(i, i + this.config.batchSize);
      await this.db.executeBatch(query, batch);
      this.progressBar.update(Math.min(i + this.config.batchSize, this.config.comments));
    }
    
    this.progressBar.stop();
    
    console.log(colors.green(`✅ 댓글 ${comments.length}개 생성 완료`));
  }

  async showCompletionSummary() {
    console.log(colors.blue('\n🎉 데이터 시딩 완료!'));
    console.log(colors.white('='.repeat(50)));
    
    // 최종 통계
    const categoryCount = await this.db.getTableCount('categories');
    const postCount = await this.db.getTableCount('posts');
    const commentCount = await this.db.getTableCount('comments');
    
    console.log(colors.cyan(`📁 카테고리: ${categoryCount.toLocaleString()}개`));
    console.log(colors.cyan(`📝 게시글: ${postCount.toLocaleString()}개`));
    console.log(colors.cyan(`💬 댓글: ${commentCount.toLocaleString()}개`));
    console.log(colors.white('='.repeat(50)));
    
    // 게시글 상태별 통계
    const stateStats = await this.db.execute(`
      SELECT state, COUNT(*) as count 
      FROM posts 
      GROUP BY state 
      ORDER BY count DESC
    `);
    
    console.log(colors.yellow('\n📊 게시글 상태별 통계:'));
    stateStats.rows.forEach(stat => {
      console.log(colors.white(`  ${stat.STATE}: ${stat.COUNT.toLocaleString()}개`));
    });
    
    // 카테고리별 게시글 수 (상위 10개)
    const categoryStats = await this.db.execute(`
      SELECT c.name, COUNT(p.id) as post_count
      FROM categories c
      LEFT JOIN posts p ON c.id = p.category_id
      GROUP BY c.id, c.name
      ORDER BY post_count DESC
      FETCH FIRST 10 ROWS ONLY
    `);
    
    console.log(colors.yellow('\n📈 카테고리별 게시글 수 (상위 10개):'));
    categoryStats.rows.forEach(stat => {
      console.log(colors.white(`  ${stat.NAME}: ${stat.POST_COUNT}개`));
    });
    
    // 댓글 많은 게시글 (상위 5개)
    const popularPosts = await this.db.execute(`
      SELECT p.title, COUNT(c.id) as comment_count
      FROM posts p
      LEFT JOIN comments c ON p.id = c.post_id
      GROUP BY p.id, p.title
      ORDER BY comment_count DESC
      FETCH FIRST 5 ROWS ONLY
    `);
    
    console.log(colors.yellow('\n🔥 댓글이 많은 게시글 (상위 5개):'));
    popularPosts.rows.forEach(post => {
      const title = post.TITLE.length > 40 ? post.TITLE.substring(0, 37) + '...' : post.TITLE;
      console.log(colors.white(`  ${title}: ${post.COMMENT_COUNT}개`));
    });
    
    console.log(colors.green('\n✨ 스트레스 테스트를 위한 데이터 준비가 완료되었습니다!'));
    console.log(colors.blue('이제 stress-test/run-tests.sh를 실행하여 테스트를 시작할 수 있습니다.'));
  }
}

// 스크립트 실행
if (require.main === module) {
  const seeder = new DataSeeder();
  seeder.run().catch(console.error);
}

module.exports = DataSeeder; 