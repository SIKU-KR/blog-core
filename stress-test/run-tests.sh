#!/bin/bash

# 블로그 퍼블릭 API 스트레스 테스트 실행 스크립트

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
    echo -e "${BLUE} 블로그 퍼블릭 API 스트레스 테스트${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_step() {
    echo -e "${GREEN}[단계 $1/${2}] $3${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 환경 확인
check_prerequisites() {
    print_step "1" "6" "환경 확인 중..."
    
    # K6 설치 확인
    if ! command -v k6 &> /dev/null; then
        print_error "K6가 설치되지 않았습니다. 다음 명령어로 설치하세요:"
        echo "  - macOS: brew install k6"
        echo "  - Ubuntu: sudo apt-get update && sudo apt-get install k6"
        echo "  - Manual: https://k6.io/docs/getting-started/installation/"
        exit 1
    fi
    
    # 서버 상태 확인
    if ! curl -s http://localhost:8080/categories > /dev/null 2>&1; then
        print_warning "서버가 실행되지 않거나 접근할 수 없습니다."
        echo "서버를 먼저 실행해주세요: ./gradlew bootRun"
        read -p "서버가 실행 중입니까? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
    
    echo "✅ 환경 확인 완료"
}

# 결과 디렉토리 생성
setup_directories() {
    print_step "2" "6" "결과 디렉토리 설정..."
    
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    RESULT_DIR="stress-test/results/${TIMESTAMP}"
    mkdir -p "${RESULT_DIR}"
    
    echo "결과 저장 위치: ${RESULT_DIR}"
}

# 베이스라인 테스트
run_baseline_test() {
    print_step "3" "6" "베이스라인 테스트 실행..."
    print_warning "단일 사용자로 기본 기능 검증 중..."
    
    k6 run \
        --vus 1 \
        --duration 30s \
        --out json="${RESULT_DIR}/baseline-test.json" \
        stress-test/k6-scenarios/normal-visitor.js || {
        print_error "베이스라인 테스트 실패"
        exit 1
    }
    
    echo "✅ 베이스라인 테스트 완료"
}

# 일반 방문자 시나리오 테스트
run_normal_visitor_test() {
    print_step "4" "6" "일반 방문자 시나리오 테스트 실행..."
    print_warning "점진적 부하 증가 (최대 100명 동시 사용자)"
    
    k6 run \
        --out json="${RESULT_DIR}/normal-visitor.json" \
        stress-test/k6-scenarios/normal-visitor.js || {
        print_error "일반 방문자 테스트 실패"
        exit 1
    }
    
    echo "✅ 일반 방문자 테스트 완료"
}

# 댓글 중심 부하 테스트
run_comment_heavy_test() {
    print_step "5" "6" "댓글 중심 부하 테스트 실행..."
    print_warning "댓글 작성/조회 집중 테스트 (최대 100명 동시 사용자)"
    
    k6 run \
        --out json="${RESULT_DIR}/comment-heavy.json" \
        stress-test/k6-scenarios/comment-heavy.js || {
        print_error "댓글 중심 테스트 실패"
        exit 1
    }
    
    echo "✅ 댓글 중심 테스트 완료"
}

# 스파이크 테스트
run_spike_test() {
    print_step "6" "6" "스파이크 테스트 실행..."
    print_warning "급격한 트래픽 증가 시뮬레이션 (최대 300명 동시 사용자)"
    
    read -p "스파이크 테스트를 실행하시겠습니까? 시스템에 부하가 클 수 있습니다. (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        k6 run \
            --out json="${RESULT_DIR}/spike-test.json" \
            stress-test/k6-scenarios/spike-test.js || {
            print_error "스파이크 테스트 실패"
            exit 1
        }
        echo "✅ 스파이크 테스트 완료"
    else
        echo "스파이크 테스트를 건너뜁니다."
    fi
}

# 결과 요약
summarize_results() {
    echo
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE} 테스트 결과 요약${NC}"
    echo -e "${BLUE}================================================${NC}"
    
    echo "📁 결과 파일 위치: ${RESULT_DIR}"
    echo
    echo "📊 생성된 파일들:"
    ls -la "${RESULT_DIR}/"
    echo
    echo "💡 다음 단계:"
    echo "  1. JSON 결과 파일을 분석하여 성능 지표 확인"
    echo "  2. Grafana 대시보드로 시각화 (선택사항)"
    echo "  3. 병목 지점 분석 및 최적화 계획 수립"
    echo "  4. 프로덕션 환경에서 추가 테스트"
}

# 메인 실행 함수
main() {
    print_header
    
    # 사용자 확인
    echo "스트레스 테스트를 시작합니다."
    echo "테스트 중에는 시스템 리소스 사용량이 높아질 수 있습니다."
    echo
    read -p "계속 진행하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "테스트를 취소합니다."
        exit 0
    fi
    
    # 테스트 실행
    check_prerequisites
    setup_directories
    run_baseline_test
    
    # 잠시 대기 (서버 안정화)
    echo "서버 안정화를 위해 10초 대기..."
    sleep 10
    
    run_normal_visitor_test
    
    # 잠시 대기
    echo "다음 테스트를 위해 15초 대기..."
    sleep 15
    
    run_comment_heavy_test
    
    # 잠시 대기
    echo "다음 테스트를 위해 20초 대기..."
    sleep 20
    
    run_spike_test
    
    summarize_results
    
    echo
    echo -e "${GREEN}🎉 모든 스트레스 테스트가 완료되었습니다!${NC}"
}

# 스크립트 실행
main "$@" 