# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Spring Boot + MyBatis + MySQL을 사용한 Java 웹 애플리케이션입니다. 관리자(Admin), 사용자(User), 공지(Notice) 도메인을 중심으로 한 CRUD API를 제공합니다.

## 개발 명령어

### 빌드 및 실행
```bash
# 애플리케이션 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# JAR 파일로 실행 (빌드 결과: demo.jar)
java -jar build/libs/demo.jar
```

### 테스트
```bash
# 테스트 실행
./gradlew test

# 모든 검증 실행 (테스트 + 코드 포맷 검사)
./gradlew check
```

### 코드 품질 관리
```bash
# Spotless를 사용한 코드 포맷팅
./gradlew spotlessApply

# 코드 포맷팅 검사
./gradlew spotlessCheck

# 의존성 버전 업데이트 확인
./gradlew dependencyUpdates
```

### Docker 실행
```bash
# Docker 이미지 빌드 및 실행
docker build -t demo-app .
docker run -p 8000:8000 demo-app
```

## 아키텍처 구조

### 패키지 구조
- `com.github.bestheroz.demo`: 비즈니스 로직 (Admin, User, Notice 도메인)
  - `controllers`: REST API 컨트롤러
  - `services`: 비즈니스 로직 서비스
  - `repository`: MyBatis 매퍼 인터페이스 (`MybatisRepository<T>` 확장)
  - `domain`: 엔티티 클래스
  - `domain/service`: 도메인 헬퍼 클래스 (예: `OperatorHelper`)
  - `dtos`: 데이터 전송 객체
- `com.github.bestheroz.standard`: 공통 프레임워크 코드
  - `common`: 공통 유틸리티 및 설정
  - `config`: Spring 설정 클래스

### 주요 기술 스택
- **Java 25** with Spring Boot 4.0.1
- **MyBatis** 4.0.1 + mybatis-repository 0.8.1 for ORM
- **Virtual Threads** 활성화 (`spring.threads.virtual.enabled: true`)
- **MySQL** 데이터베이스
- **JWT** 인증/인가 (Auth0 java-jwt 4.5.0)
- **Swagger/OpenAPI** API 문서화 (SpringDoc 3.0.1)
- **Spotless** 코드 포맷팅 (Google Java Format)
- **P6Spy** SQL 로깅
- **Sentry** 에러 모니터링 (8.29.0)
- **HikariCP** 커넥션 풀

### 인증/보안
- JWT 토큰 기반 인증 (`JwtTokenProvider`, `JwtAuthenticationFilter`)
- Access Token: 5분 (local: 1440분)
- Refresh Token: 30분
- Spring Security 설정: `SecurityConfig`
- CORS 설정: http://localhost:3000 허용
- BCrypt 패스워드 인코딩

### API 접근
- 기본 포트: 8000
- Swagger UI: http://localhost:8000/swagger-ui.html
- API Docs: http://localhost:8000/v3/api-docs
- 공개 엔드포인트 (인증 불필요):
  - GET: `/api/v1/health/**`, `/api/v1/notices`, `/api/v1/admins/check-login-id`, `/api/v1/users/check-login-id`
  - POST: `/api/v1/admins/login`, `/api/v1/users/login`

### 데이터베이스
- 환경별 설정 (local, sandbox, qa, prod)
- 마이그레이션: 프로젝트 루트의 `/migration` 디렉토리 (V1, V2, V3 SQL 파일)
- HikariCP 연결 풀 설정
  - local: maximum-pool-size=3, minimum-idle=2
  - sandbox/qa: maximum-pool-size=10, minimum-idle=5
  - prod: maximum-pool-size=30, minimum-idle=10

### Repository 패턴
- `MybatisRepository<T>` 인터페이스 확장으로 CRUD 자동 생성
- 조건부 쿼리: `getItemByMap()`, `getItemsByMapOrderByLimitOffset()`, `countByMap()`
- 필터 조건: `"field:contains"`, `"field:in"`, `"field:not"` 등 연산자 지원
- 예시: `Map.of("loginId", value, "removedFlag", false, "id:not", excludeId)`

### 공통 기능
- 전역 예외 처리: `ApiExceptionHandler`
- 로깅: `TraceLogger`, `LogUtils`
- 응답 래퍼: `ApiResult`, `Result`
- 공통 도메인: `IdCreated`, `IdCreatedUpdated` (생성자/수정자 추적)
- 열거형 처리: `GenericEnumTypeHandler`
- Virtual Threads 기반 병렬 처리: `Executors.newVirtualThreadPerTaskExecutor()` 사용

## 개발 가이드라인

### 새 도메인 추가시
1. `domain` 패키지에 엔티티 클래스 생성
2. `repository` 패키지에 MyBatis 매퍼 인터페이스 생성
3. `services` 패키지에 비즈니스 로직 서비스 생성
4. `controllers` 패키지에 REST API 컨트롤러 생성
5. `dtos` 패키지에 하위 폴더를 만들고 DTO 클래스들 생성
6. 필요시 `domain/service` 패키지에 헬퍼 클래스 생성 (예: `OperatorHelper`)

### 트랜잭션 경계 원칙

**올바른 패턴**:
- Controller → Service (with @Transactional) → Repository
- Controller → Service (with @Transactional) → Helper Service (without @Transactional)
- Service (with @Transactional) → Private methods (without @Transactional)

**피해야 할 패턴**:
- Service (with @Transactional) → Service (with @Transactional)
- Helper Service에 @Transactional 사용
- Private 메서드에 @Transactional 사용

### 도메인 헬퍼 패턴
- 도메인별 공통 로직은 `domain/service` 패키지의 헬퍼 클래스로 분리
- 헬퍼 클래스는 @Transactional을 사용하지 않음
- 예시: `OperatorHelper` - 운영자(Admin/User) 공통 처리 로직

### 테스트
현재 테스트 코드가 없으므로, 새로운 테스트 작성시 Spring Boot Test 규칙을 따르세요.

### 코드 스타일
- Google Java Format 사용 (Spotless 플러그인)
- Lombok 어노테이션 활용
- 한글 주석 허용