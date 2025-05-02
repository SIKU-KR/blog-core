# 개인 블로그 백엔드 서버

Spring Boot 기반의 개인 블로그 백엔드 API 서버입니다.

## 기술 스택

- Java 17
- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- H2 Database
- Lombok
- SpringDoc OpenAPI (Swagger)

## 주요 기능

- 사용자 인증 및 권한 관리 (Spring Security)
- RESTful API 제공
- 데이터베이스 연동 (JPA)
- API 문서화 (Swagger)

## 시작하기

### 요구사항

- Java 17 이상
- Gradle

### 실행 방법

```bash
./gradlew bootRun
```

### API 문서

서버 실행 후 다음 URL에서 Swagger UI를 통해 API 문서를 확인할 수 있습니다:
```
http://localhost:8080/swagger-ui.html
```

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 