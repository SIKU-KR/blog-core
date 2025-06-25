const oracledb = require('oracledb');
require('dotenv').config();

class Database {
  constructor() {
    this.connection = null;
  }

  async connect() {
    try {
      // Oracle 클라이언트 라이브러리 초기화
      oracledb.outFormat = oracledb.OUT_FORMAT_OBJECT;
      oracledb.autoCommit = true;

      // Oracle Cloud Autonomous Database 연결 설정 (Wallet 없이)
      const connectionConfig = {
        user: process.env.ORACLE_USERNAME,
        password: process.env.ORACLE_PASSWORD,
        connectString: process.env.ORACLE_CONNECT_STRING,
        
        // 연결 풀 설정
        poolMin: 1,
        poolMax: 10,
        poolIncrement: 1,
        poolTimeout: 300,
        poolPingInterval: 60
      };

      console.log('🔐 Wallet 없이 TCPS 직접 연결 시도...');

      this.connection = await oracledb.getConnection(connectionConfig);
      
      console.log('✅ Oracle Cloud Autonomous Database TLS 연결 성공');
      console.log(`📊 연결된 서비스: ${connectionConfig.connectString.match(/service_name=([^)]+)/)?.[1] || 'Unknown'}`);
      
      return this.connection;
    } catch (error) {
      console.error('❌ Oracle Cloud Database 연결 실패:', error.message);
      
      // 상세한 오류 정보 제공
      if (error.message.includes('ORA-12541')) {
        console.error('💡 TNS 리스너 오류: 호스트나 포트를 확인하세요.');
      } else if (error.message.includes('ORA-12514')) {
        console.error('💡 서비스명 오류: service_name을 확인하세요.');
      } else if (error.message.includes('ORA-01017')) {
        console.error('💡 인증 오류: 사용자명/비밀번호를 확인하세요.');
      } else if (error.message.includes('ORA-28759')) {
        console.error('💡 SSL 오류: Wallet 파일이나 SSL 설정을 확인하세요.');
      }
      
      throw error;
    }
  }

  async disconnect() {
    if (this.connection) {
      await this.connection.close();
      console.log('🔌 Oracle Cloud Database 연결 종료');
    }
  }

  async execute(query, params = []) {
    if (!this.connection) {
      throw new Error('데이터베이스 연결이 없습니다.');
    }
    
    try {
      const result = await this.connection.execute(query, params, {
        autoCommit: true,
        outFormat: oracledb.OUT_FORMAT_OBJECT
      });
      return result;
    } catch (error) {
      console.error('SQL 실행 오류:', error.message);
      console.error('쿼리:', query);
      console.error('파라미터:', params);
      throw error;
    }
  }

  async executeBatch(query, batchData) {
    if (!this.connection) {
      throw new Error('데이터베이스 연결이 없습니다.');
    }

    try {
      const result = await this.connection.executeMany(query, batchData, {
        autoCommit: true,
        batchErrors: true
      });
      return result;
    } catch (error) {
      console.error('배치 실행 오류:', error.message);
      throw error;
    }
  }

  async getTableCount(tableName) {
    const result = await this.execute(`SELECT COUNT(*) as COUNT FROM ${tableName}`);
    return result.rows[0].COUNT;
  }

  async clearTable(tableName) {
    // Oracle Autonomous Database에서는 TRUNCATE 권한이 제한될 수 있음
    try {
      await this.execute(`TRUNCATE TABLE ${tableName}`);
      console.log(`🧹 ${tableName} 테이블 TRUNCATE 완료`);
    } catch (error) {
      if (error.message.includes('ORA-00942') || error.message.includes('insufficient privileges')) {
        // TRUNCATE 권한이 없으면 DELETE 사용
        await this.execute(`DELETE FROM ${tableName}`);
        console.log(`🧹 ${tableName} 테이블 DELETE 완료`);
      } else {
        throw error;
      }
    }
  }

  async resetSequence(sequenceName, startValue = 1) {
    // Oracle Autonomous Database에서 시퀀스 초기화
    try {
      // 현재 시퀀스 값 확인
      const currentResult = await this.execute(`SELECT ${sequenceName}.CURRVAL as CURRENT_VAL FROM DUAL`);
      const currentVal = currentResult.rows[0]?.CURRENT_VAL;
      
      if (currentVal && currentVal > startValue) {
        // 시퀀스를 음수로 증가시켜서 초기화
        const decrementValue = currentVal - startValue + 1;
        await this.execute(`ALTER SEQUENCE ${sequenceName} INCREMENT BY -${decrementValue}`);
        await this.execute(`SELECT ${sequenceName}.NEXTVAL FROM DUAL`);
        await this.execute(`ALTER SEQUENCE ${sequenceName} INCREMENT BY 1`);
      }
      
      console.log(`🔄 ${sequenceName} 시퀀스 초기화 완료 (시작값: ${startValue})`);
    } catch (error) {
      console.warn(`⚠️ 시퀀스 ${sequenceName} 초기화 실패:`, error.message);
      // 시퀀스가 존재하지 않거나 권한이 없을 수 있음
    }
  }

  // Oracle Cloud Database 상태 확인
  async checkConnection() {
    try {
      const result = await this.execute('SELECT 1 as PING FROM DUAL');
      console.log('🏓 데이터베이스 연결 상태: 정상');
      return true;
    } catch (error) {
      console.error('💔 데이터베이스 연결 상태: 오류', error.message);
      return false;
    }
  }
}

module.exports = Database; 