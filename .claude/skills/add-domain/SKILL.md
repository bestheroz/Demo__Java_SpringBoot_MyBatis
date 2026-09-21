---
name: add-domain
description: 이 저장소에 새 비즈니스 도메인(엔티티 + Repository + Service + Controller + DTO)을 추가할 때 사용한다. "새 도메인 추가", "새 API 만들기", 기존 Admin/User/Notice 와 같은 계층 구조의 CRUD 를 새로 만들어야 할 때 호출한다.
---

# 새 도메인 추가 절차

`com.github.bestheroz.demo` 는 도메인 폴더가 아니라 계층 폴더로 나뉘어 있다.
한 도메인을 추가하면 아래 5~6개 파일이 각기 다른 폴더에 흩어져 생긴다.
기존 `Notice` 가 가장 단순한 참고 대상이고, `Admin` 이 인증까지 포함한 전체 예시다.

## 1. 스키마 먼저

`migration/` 에 `V<다음번호>__Create_<복수형>.sql` 을 추가한다.
기존 V1~V3 의 공통 컬럼(`id`, `created_at`, `created_object_type`, `created_object_id`, `updated_*`, `removed_flag`)을 그대로 따른다.
자동 실행되지 않으므로 로컬 DB 에 직접 적용한 뒤 다음 단계로 간다.

## 2. 엔티티 — `demo/domain/<Name>.java`

- 생성자 추적만 필요하면 `IdCreated`, 수정자까지 필요하면 `IdCreatedUpdated` 를 상속한다.
- 매핑은 필드에 `jakarta.persistence` 의 `@Column` 을 붙이는 것뿐이다. `@Entity` / `@Table` 은 쓰지 않으며 테이블명은 클래스명에서 유도된다.
- Lombok 조합은 루트 CLAUDE.md "코드 스타일" 을 따른다.
- 생성/수정 팩토리 메서드를 엔티티 안에 두고 `Operator` 를 받아 감사 필드를 채운다 (`Notice.of` 참고).

## 3. Repository — `demo/repository/<Name>Repository.java`

```java
@Mapper
@Repository
public interface <Name>Repository extends MybatisRepository<<Name>> {}
```

CRUD 는 자동 생성되므로 메서드를 추가하지 않는다.
조건 조회는 맵 기반 메서드와 연산자 접미사로 처리한다 — 루트 CLAUDE.md "Repository 사용 시 함정" 을 먼저 읽는다.

## 4. DTO — `demo/dtos/<name>/`

용도별로 파일을 나누고(`<Name>Dto`, `<Name>CreateDto`, `<Name>UpdateDto` …) 각 파일 안에 `Request` / `Response` 를 중첩 `static class` 로 둔다.
조회 `Response` 는 `IdCreatedUpdatedDto` 를 상속하고 엔티티를 받는 `of` 정적 팩토리를 갖는다.

## 5. Service — `demo/services/<Name>Service.java`

- `@Service` + `@RequiredArgsConstructor` + 클래스 레벨 `@Transactional(readOnly = true)`.
- 쓰기 메서드에만 `@Transactional` 을 다시 붙인다.
- 다른 도메인의 Service 를 호출하지 않는다. 공통 로직이 필요하면 7번으로 간다.
- 트랜잭션 규칙 전체는 루트 CLAUDE.md "트랜잭션 경계" 에 있다.

## 6. Controller — `demo/controllers/<Name>Controller.java`

- `@RestController` + `@RequestMapping("api/v1/<복수형>")` + `@Tag` 로 Swagger 그룹을 만든다.
- 현재 로그인 주체가 필요하면 `@CurrentUser Operator operator` 를 받는다.
- 인증이 필요한 엔드포인트에는 `@SecurityRequirement(name = "bearerAuth")` 와 `@PreAuthorize("hasAuthority('<PERMISSION>')")` 를 붙인다.
- 성공 응답은 DTO 를 그대로 반환한다 (`ApiResult` 로 감싸지 않는다).
- 인증 없이 열어야 하는 경로가 있으면 `SecurityConfig` 의 public 배열에 추가한다.

## 7. (선택) 헬퍼 — `demo/domain/service/<Name>Helper.java`

여러 도메인이 공유하는 로직만 여기에 둔다. `@Transactional` 을 붙이지 않는다. `OperatorHelper` 가 예시다.

## 8. 마무리

- `./gradlew check` 로 컴파일·포맷·테스트를 함께 확인한다.
- 이 도메인 전용 규칙이 생겼다면 루트 CLAUDE.md 가 아니라 `.claude/rules/` 에 두고 "도메인 인덱스" 에 한 줄 추가한다.
