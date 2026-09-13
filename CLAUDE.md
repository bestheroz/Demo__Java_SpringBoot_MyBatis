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

# 의존성·플러그인 버전은 gradle/libs.versions.toml(버전 카탈로그). 버전 없는 항목은 Spring Boot BOM 관리
./gradlew dependencyUpdates                    # ben-manes 리포트, 사전 릴리스 포함(Demo 정책)
./gradlew versionCatalogUpdate --interactive   # 올릴 후보를 gradle/libs.versions.updates.toml 에 쓴다
./gradlew versionCatalogApplyUpdates           # 위 파일에 남긴 항목만 카탈로그에 반영
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
- **Java 25** with Spring Boot 4.2.0-M1 (Gradle 9.8.0-rc-1)
- **Jackson 3.1.5** (`tools.jackson`) - Spring Boot 4.2 기본 JSON 처리
- **MyBatis** 4.1.0 + mybatis-repository 0.10.1 for ORM (엔티티 매핑용 jakarta.persistence-api 4.0.0-M6)
- **Virtual Threads** 활성화 (`spring.threads.virtual.enabled: true`)
- **MySQL** 데이터베이스 (mysql-connector-j 26.7.0, BOM 보다 앞선 명시 버전)
- **JWT** 인증/인가 (Auth0 java-jwt 4.6.1)
- **Swagger/OpenAPI** API 문서화 (SpringDoc 3.1.1)
- **Spotless** 코드 포맷팅 (Google Java Format, 8.10.2)
- **P6Spy** SQL 로깅 (2.0.1)
- **Sentry** 에러 모니터링 (8.56.0)
- **HikariCP** 커넥션 풀

### 의존성 관리
- **Demo 정책**: 신규 버전 선체험과 변화점 발견이 목적이라 사전 릴리스(M·RC·Beta·Alpha 등)를 허용하고 우선한다. `versionCatalogUpdate` 선택기는 `LATEST`, `dependencyUpdates` 는 `rejectPreReleases = false` 다.
- 좌표는 전부 `gradle/libs.versions.toml` 에 있고 `build.gradle` 은 `libs.xxx` / `alias(libs.plugins.xxx)` 로만 참조한다.
- 버전 없이 넣은 항목(spring-boot-starter-*, lombok, aspectjweaver)은 `{ module = "g:a" }` 로 두어 Spring Boot BOM 을 따른다. Boot 플러그인을 사전 릴리스 포함 최신으로 올리면 함께 따라간다.
- BOM 이 관리하지만 BOM 보다 앞서 체험하려고 버전을 적은 항목(mysql-connector-j, jakarta.persistence-api)은 명시 버전을 유지한 채 최신으로 올린다. 직접 적은 버전은 BOM 을 이긴다. BOM 관리 좌표에 버전을 새로 적는 것은 그 의도가 있을 때만 한다.
- `versionCatalogUpdate` 는 버전 없는 항목을 건너뛰고 버전 있는 항목만 사전 릴리스 포함 최신으로 올린다.
- 올린 버전이 깨지면 먼저 코드를 고친다. 고칠 수 없는 좌표만 동작하는 최신 버전으로 내리고 카탈로그 항목 바로 위 줄에 `# @pin` 을 달며, 사유는 `build.gradle` 주석에 적는다.
- `versionCatalogUpdate` 가 카탈로그를 다시 쓸 때 항목 옆 주석을 지운다. 남겨야 할 설명은 `build.gradle` 에 두고, 카탈로그에는 `@pin` / `@keep` 애노테이션만 쓴다.
- `gradle.properties` 가 설정 캐시(`org.gradle.configuration-cache`)·빌드 캐시(`org.gradle.caching`)·병렬 실행(`org.gradle.parallel`)을 켠다. 그래서 CI 워크플로의 gradlew 명령에는 이 플래그를 따로 주지 않는다. `versionCatalogUpdate` 는 설정 캐시와 호환되지 않아 실행할 때마다 캐시 항목이 버려진다(빌드는 성공한다).

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
- 엔티티 경로(`insert`, `updateById`)에서 null 필드는 "정하지 않았다"는 뜻이다. INSERT 는 그 자리에 `DEFAULT` 를 내고 UPDATE 는 SET 에서 뺀다. 컬럼을 NULL 로 비우려면 `updateMapById(map, id)` 에 null 값을 담아 넘기고, `Map.of` 는 null 값을 받지 않으므로 `HashMap` 을 쓴다(logout 의 token 비우기가 그 예)

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