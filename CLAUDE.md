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

# JAR 파일로 실행
java -jar build/libs/demo.jar
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
  - `repository`: MyBatis 매퍼 인터페이스
  - `domain`: 엔티티 클래스
  - `dtos`: 데이터 전송 객체
- `com.github.bestheroz.standard`: 공통 프레임워크 코드
  - `common`: 공통 유틸리티 및 설정
  - `config`: Spring 설정 클래스

### 주요 기술 스택
- **Java 21** with Spring Boot 3.5.4
- **MyBatis** 3.0.5 for ORM
- **MySQL** 데이터베이스
- **JWT** 인증/인가
- **Swagger/OpenAPI** API 문서화
- **Spotless** 코드 포맷팅
- **P6Spy** SQL 로깅
- **Sentry** 에러 모니터링

### 인증/보안
- JWT 토큰 기반 인증 (`JwtTokenProvider`, `JwtAuthenticationFilter`)
- Access Token: 5분 (local: 1440분)
- Refresh Token: 30분
- Spring Security 설정: `SecurityConfig`

### 데이터베이스
- 환경별 설정 (local, sandbox, qa, prod)
- 기본 포트: 8000
- HikariCP 연결 풀 사용
- Flyway 마이그레이션 파일: `migration/` 디렉토리

### 공통 기능
- 전역 예외 처리: `ApiExceptionHandler`
- 로깅: `TraceLogger`, `LogUtils`
- 응답 래퍼: `ApiResult`, `Result`
- 공통 도메인: `IdCreated`, `IdCreatedUpdated`
- 열거형 처리: `GenericEnumTypeHandler`

## 개발 가이드라인

### 새 도메인 추가시
1. `domain` 패키지에 엔티티 클래스 생성
2. `repository` 패키지에 MyBatis 매퍼 인터페이스 생성
3. `services` 패키지에 비즈니스 로직 서비스 생성
4. `controllers` 패키지에 REST API 컨트롤러 생성
5. `dtos` 패키지에 하위 폴더를 만들고 DTO 클래스들 생성

### 테스트
현재 테스트 코드가 없으므로, 새로운 테스트 작성시 Spring Boot Test 규칙을 따르세요.

### 코드 스타일
- Google Java Format 사용 (Spotless 플러그인)
- Lombok 어노테이션 활용
- 한글 주석 허용