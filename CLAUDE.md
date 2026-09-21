# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Spring Boot + MyBatis + MySQL 기반 CRUD API. 도메인 구성은 아래 "도메인 인덱스" 참고.

## 개발 명령어

```bash
./gradlew bootRun          # 로컬 실행 (포트 8000, Swagger UI /swagger-ui.html)
./gradlew build            # 빌드 산출물 build/libs/demo.jar
./gradlew check            # 테스트 + 포맷 검사를 한 번에. 작업 종료 전 이걸로 검증한다
./gradlew spotlessApply    # 포맷 교정 (Google Java Format)
```

의존성 갱신은 3단계로만 한다. 중간 파일 `gradle/libs.versions.updates.toml` 은 커밋 대상이 아니다.

```bash
./gradlew dependencyUpdates                    # 사전 릴리스 포함 리포트
./gradlew versionCatalogUpdate --interactive   # 올릴 후보를 updates.toml 에 기록
./gradlew versionCatalogApplyUpdates           # updates.toml 에 남긴 항목만 카탈로그에 반영
```

## 의존성 관리

- **Demo 정책**: 신규 버전 선체험과 변화점 발견이 목적이라 사전 릴리스(M·RC·Beta·Alpha 등)를 허용하고 우선한다. `versionCatalogUpdate` 선택기는 `LATEST`, `dependencyUpdates` 는 `rejectPreReleases = false` 다.
- 좌표는 전부 `gradle/libs.versions.toml` 에 있고 `build.gradle` 은 `libs.xxx` / `alias(libs.plugins.xxx)` 로만 참조한다.
- 버전 없이 넣은 항목은 `{ module = "g:a" }` 로 두어 Spring Boot BOM 을 따른다. Boot 플러그인을 올리면 함께 따라간다. `versionCatalogUpdate` 는 이런 항목을 건너뛴다.
- BOM 이 관리하지만 BOM 보다 앞서 체험하려고 버전을 적은 항목은 명시 버전을 유지한 채 최신으로 올린다. 직접 적은 버전은 BOM 을 이긴다. BOM 관리 좌표에 버전을 새로 적는 것은 그 의도가 있을 때만 한다.
- 올린 버전이 깨지면 먼저 코드를 고친다. 고칠 수 없는 좌표만 동작하는 최신 버전으로 내리고 카탈로그 항목 바로 위 줄에 `# @pin` 을 달며, 사유는 `build.gradle` 주석에 적는다.
- `versionCatalogUpdate` 가 카탈로그를 다시 쓸 때 항목 옆 주석을 지운다. 남겨야 할 설명은 `build.gradle` 에 두고, 카탈로그에는 `@pin` / `@keep` 애노테이션만 쓴다.
- `gradle.properties` 가 설정 캐시·빌드 캐시·병렬 실행을 켠다. 그래서 CI 워크플로의 gradlew 명령에는 이 플래그를 따로 주지 않는다. `versionCatalogUpdate` 는 설정 캐시와 호환되지 않아 실행할 때마다 캐시 항목이 버려진다(빌드는 성공한다).

## 트랜잭션 경계

`@Transactional` 은 `services` 의 서비스 클래스에만 붙인다. 중첩 경계와 프록시 미적용을 막기 위해서다.

- 허용: Controller → Service(`@Transactional`) → Repository / Helper(무 `@Transactional`) / private 메서드
- 금지: Service(`@Transactional`) → 다른 Service(`@Transactional`)
- 금지: `domain/service` 의 헬퍼 클래스에 `@Transactional`
- 금지: private 메서드에 `@Transactional` — 자기 호출이라 프록시를 타지 않아 조용히 무시된다

도메인 간 공통 로직이 필요하면 서비스를 서로 호출하지 말고 `domain/service` 에 헬퍼를 두고 각 서비스가 호출한다.

## Repository 사용 시 함정

- 엔티티 경로(`insert`, `updateById`)에서 null 필드는 "정하지 않았다"는 뜻이다. INSERT 는 그 자리에 `DEFAULT` 를 내고 UPDATE 는 SET 에서 뺀다.
- 컬럼을 NULL 로 비우려면 `updateMapById(map, id)` 에 null 값을 담아 넘긴다. `Map.of` 는 null 값을 거부하므로 `HashMap` 을 쓴다 (logout 의 token 비우기가 그 예).
- 조건 맵은 `"field:contains"`, `"field:in"`, `"field:not"` 연산자 접미사를 지원한다. 예: `Map.of("loginId", value, "removedFlag", false, "id:not", excludeId)`

## 인증/보안

- 인증 제외 경로는 `SecurityConfig` 의 `GET_PUBLIC` / `POST_PUBLIC` / `DELETE_PUBLIC` 배열에만 추가한다. 다른 곳에 permitAll 을 흩뿌리지 않는다.
- Access Token 5분 / Refresh Token 30분. **local·sandbox 프로파일만 Access 1440분**이라 만료 관련 버그는 local 에서 재현되지 않는다. 검증은 prod 설정값으로 한다.
- 비밀번호는 `PasswordUtil` 을 거친다. BCrypt 인코더를 직접 생성하지 않는다.

## 코드 스타일

- 엔티티: `@Data` + `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + `@AllArgsConstructor(access = AccessLevel.PRIVATE)`. `IdCreated` / `IdCreatedUpdated` 를 상속하면 `@EqualsAndHashCode(callSuper = true)` 를 함께 붙인다. 매핑은 필드의 `@Column` 뿐이고 `@Entity` / `@Table` 은 쓰지 않는다 — 테이블명은 클래스명에서 유도된다.
- DTO: 용도별 파일(`<Name>Dto`, `<Name>CreateDto` …) 안에 `Request` / `Response` 를 중첩 `static class` 로 두고 각각에 `@Data` 를 붙인다. `Request`·`Response` 마다 파일을 새로 만들지 않는다.
- Service·Controller: `@RequiredArgsConstructor` 로 생성자 주입한다. `@Autowired` 필드 주입은 쓰지 않는다. 로깅은 `@Slf4j`.
- 컨트롤러는 DTO 를 그대로 반환하고 목록만 `ListResult` 로 감싼다. `ApiResult` 는 `ApiExceptionHandler` 의 에러 응답 전용이라 성공 응답을 감싸지 않는다.
- 실패는 `RequestException400` 등 코드별 예외를 던져 `ApiExceptionHandler` 가 변환하게 둔다. 컨트롤러에서 `ResponseEntity` 를 직접 조립하지 않는다.
- 병렬 처리는 `Executors.newVirtualThreadPerTaskExecutor()` 를 쓴다. 플랫폼 스레드 풀을 새로 만들지 않는다.
- 한글 주석 허용. 포맷은 저장 후 `./gradlew spotlessApply` 가 맞춘다 (Java 편집 시 hook 이 자동 실행).

## 테스트

- 테스트는 `src/test/java` 에 대상 클래스명 + `Test` 로 둔다.
- 슬라이스 테스트를 기본으로 한다(`@WebMvcTest`, `@MybatisTest`). 전체 컨텍스트가 필요한 경우에만 `@SpringBootTest` 를 쓴다.
- `@Disabled` 나 빈 테스트 본문으로 통과시키지 않는다.

## 데이터베이스 변경

스키마 변경은 `migration/` 에 `V<번호>__<설명>.sql` 로 추가한다. **Flyway·Liquibase 가 없어 자동 실행되지 않는다.** 파일명은 관례를 빌린 것뿐이고, 코드 배포 전에 사람이 직접 적용해야 한다.

## CLAUDE.md 관리 규칙
- 이 파일은 200줄 이하 유지. 매 세션 필요한 내용만 둔다: 빌드/테스트 명령, 전역 컨벤션, 도메인 간 의존 규칙, 함정과 그 이유
- 코드에서 유추 가능한 내용(디렉터리 구조, 의존성 목록, 아키텍처 개요)은 쓰지 않는다
- 지시는 검증 가능한 수준으로 구체적으로 쓴다 (X "포맷 잘 맞춰라" / O "2-space 들여쓰기")
- 특정 도메인/경로에만 해당하는 규칙은 이 파일에 넣지 않는다
  - 도메인이 단일 폴더로 분리돼 있으면 → 해당 폴더의 CLAUDE.md
  - 여러 폴더에 흩어져 있으면 → `.claude/rules/<topic>.md` + `paths` frontmatter
  - 다단계 절차는 → 스킬
- 하위 CLAUDE.md 와 rules 에는 루트 규칙을 재진술하지 않는다. 충돌/중복 발견 시 사용자에게 알린다
- 도메인 규칙을 분리하면 아래 "도메인 인덱스"에 한 줄 추가한다
- 지시 파일을 추가/수정할 때는 변경 전 사용자에게 위치와 내용을 먼저 제안한다

## 도메인 인덱스
<!-- 형식: `경로/` — 한 줄 설명, 규칙 파일 위치 -->
- `src/main/java/com/github/bestheroz/demo/` — Admin·User·Notice 비즈니스 도메인. 계층별 폴더에 흩어져 있어 분리 시 `.claude/rules/` + paths. 신규 도메인 추가 절차는 `add-domain` 스킬
- `src/main/java/com/github/bestheroz/standard/` — 인증·예외·응답·MyBatis 공통 프레임워크. 단일 폴더라 분리 시 해당 폴더 CLAUDE.md. 규칙 파일 없음
- `migration/` — MySQL 스키마 변경 SQL. 위 "데이터베이스 변경" 섹션
- `gradle/`, `build.gradle` — 버전 카탈로그·사전 릴리스 선체험 정책. 위 "의존성 관리" 섹션
- `.github/workflows/` — 테스트·배포·버전 동기화 CI. 규칙 파일 없음
