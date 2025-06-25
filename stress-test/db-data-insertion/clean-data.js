#!/usr/bin/env node

const Database = require('./config/database');
const colors = require('colors');
require('dotenv').config();

class DataCleaner {
  constructor() {
    this.db = new Database();
  }

  async run() {
    try {
      console.log(colors.blue('🧹 데이터베이스 정리 시작'));
      
      // 데이터베이스 연결
      await this.db.connect();
      
      // 현재 데이터 확인
      await this.showCurrentData();
      
      // 사용자 확인
      const shouldClean = await this.confirmCleaning();
      
      if (shouldClean) {
        await this.cleanAllData();
        console.log(colors.green('✅ 데이터베이스 정리 완료'));
      } else {
        console.log(colors.yellow('정리를 취소했습니다.'));
      }
      
    } catch (error) {
      console.error(colors.red('❌ 정리 과정에서 오류 발생:'), error.message);
      process.exit(1);
    } finally {
      await this.db.disconnect();
    }
  }

  async showCurrentData() {
    console.log(colors.yellow('\n📊 현재 데이터베이스 상태:'));
    
    const commentCount = await this.db.getTableCount('comments');
    const postCount = await this.db.getTableCount('posts');
    const categoryCount = await this.db.getTableCount('categories');
    
    console.log(colors.white(`카테고리: ${categoryCount.toLocaleString()}개`));
    console.log(colors.white(`게시글: ${postCount.toLocaleString()}개`));
    console.log(colors.white(`댓글: ${commentCount.toLocaleString()}개`));
    
    const totalData = commentCount + postCount + categoryCount;
    if (totalData === 0) {
      console.log(colors.green('데이터베이스가 이미 비어있습니다.'));
      process.exit(0);
    }
  }

  async confirmCleaning() {
    if (process.env.CI) {
      return true; // CI 환경에서는 자동 승인
    }
    
    const readline = require('readline').createInterface({
      input: process.stdin,
      output: process.stdout
    });
    
    const answer = await new Promise(resolve => {
      readline.question(colors.red('\n⚠️  모든 데이터를 삭제하시겠습니까? (y/N): '), resolve);
    });
    
    readline.close();
    return answer.toLowerCase() === 'y';
  }

  async cleanAllData() {
    console.log(colors.yellow('\n🧹 데이터 삭제 중...'));
    
    // 외래키 제약 조건 때문에 역순으로 삭제
    await this.db.clearTable('comments');
    await this.db.clearTable('posts');
    await this.db.clearTable('categories');
    
    // 최종 확인
    const commentCount = await this.db.getTableCount('comments');
    const postCount = await this.db.getTableCount('posts');
    const categoryCount = await this.db.getTableCount('categories');
    
    console.log(colors.green(`삭제 후 상태: 카테고리 ${categoryCount}개, 게시글 ${postCount}개, 댓글 ${commentCount}개`));
  }
}

// 스크립트 실행
if (require.main === module) {
  const cleaner = new DataCleaner();
  cleaner.run().catch(console.error);
}

module.exports = DataCleaner; 