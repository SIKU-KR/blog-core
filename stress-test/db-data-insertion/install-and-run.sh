#!/bin/bash

# 블로그 데이터 시딩 스크립트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수 정의
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE} 블로그 데이터 시딩 도구${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_step() {
    echo -e "${GREEN}[단계 $1] $2${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Node.js 및 npm 확인
check_nodejs() {
    if ! command -v node &> /dev/null; then
        print_error "Node.js가 설치되지 않았습니다."
        echo "다음 링크에서 Node.js를 설치해주세요: https://nodejs.org/"
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        print_error "npm이 설치되지 않았습니다."
        exit 1
    fi
    
    echo "✅ Node.js $(node --version) 확인 완료"
}

# 의존성 설치
install_dependencies() {
    print_step "1" "의존성 설치 중..."
    
    if [ ! -f "package.json" ]; then
        print_error "package.json 파일을 찾을 수 없습니다."
        echo "stress-test/db-data-insertion 디렉토리에서 실행해주세요."
        exit 1
    fi
    
    npm install
    echo "✅ 의존성 설치 완료"
}

# 환경 설정 확인
check_env_config() {
    print_step "2" "환경 설정 확인 중..."
    
    if [ ! -f ".env" ]; then
        print_warning ".env 파일이 없습니다. .env.example에서 복사합니다."
        cp .env.example .env
        echo "📝 .env 파일이 생성되었습니다. 데이터베이스 정보를 확인해주세요."
    fi
    
    # .env 파일 내용 표시
    echo "현재 설정:"
    grep -E "^(DB_|CATEGORIES_COUNT|POSTS_COUNT|COMMENTS_COUNT)" .env | while read line; do
        echo "  $line"
    done
}

# 데이터베이스 연결 테스트
test_database_connection() {
    print_step "3" "데이터베이스 연결 테스트..."
    
    node -e "
        const Database = require('./config/database');
        const db = new Database();
        db.connect()
          .then(() => {
            console.log('✅ 데이터베이스 연결 성공');
            return db.disconnect();
          })
          .catch((error) => {
            console.error('❌ 데이터베이스 연결 실패:', error.message);
            process.exit(1);
          });
    "
}

# 데이터 시딩 실행
run_seeding() {
    print_step "4" "데이터 시딩 실행..."
    
    echo "다음 데이터가 생성됩니다:"
    echo "  - 카테고리: $(grep CATEGORIES_COUNT .env | cut -d'=' -f2)개"
    echo "  - 게시글: $(grep POSTS_COUNT .env | cut -d'=' -f2)개"
    echo "  - 댓글: $(grep COMMENTS_COUNT .env | cut -d'=' -f2)개"
    echo
    
    # 사용자 확인
    read -p "계속 진행하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "시딩을 취소합니다."
        exit 0
    fi
    
    # 시딩 실행
    node seed-data.js
}

# 메뉴 표시
show_menu() {
    echo
    echo "사용 가능한 명령어:"
    echo "  ./install-and-run.sh            - 전체 과정 실행"
    echo "  ./install-and-run.sh install    - 의존성만 설치"
    echo "  ./install-and-run.sh seed       - 시딩만 실행"
    echo "  ./install-and-run.sh clean      - 데이터 정리"
    echo "  ./install-and-run.sh test       - 데이터베이스 연결 테스트"
    echo
}

# 메인 함수
main() {
    print_header
    
    case "${1:-all}" in
        "install")
            check_nodejs
            install_dependencies
            ;;
        "seed")
            check_nodejs
            node seed-data.js
            ;;
        "clean")
            check_nodejs
            node clean-data.js
            ;;
        "test")
            check_nodejs
            check_env_config
            test_database_connection
            ;;
        "all"|"")
            check_nodejs
            install_dependencies
            check_env_config
            test_database_connection
            run_seeding
            echo
            echo -e "${GREEN}🎉 모든 과정이 완료되었습니다!${NC}"
            echo -e "${BLUE}이제 스트레스 테스트를 실행할 수 있습니다.${NC}"
            ;;
        *)
            print_error "알 수 없는 명령어: $1"
            show_menu
            exit 1
            ;;
    esac
}

# 스크립트 실행
main "$@" 