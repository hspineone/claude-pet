# Claude Pet — Progress

> 현재 작업 중인 한 기능 사이클의 SPEC 정의.
> 기능 완료 시 다음 사이클에 맞춰 재작성됨.
> 과거 사이클은 CHANGELOG / git log / PRD 참조.

## 목표

액세서리 잠금해제 시스템의 **MVP (사이클 A.0)** 골격을 만든다. Single 모델(한 번에 1개 + 없음) 라디오로 트레이 옷장을 도입해 영속·렌더 흐름을 검증한다. 다중 동시 착용·호감도 마일스톤 게이트는 후속 사이클로 분리.

## 원칙 / 제약

- 자산은 "캐릭터 + 액세서리 합쳐진 PNG" 한 세트 → 한 번에 한 종만 적용.
- Default 상태에서만 액세서리 노출. 다른 상태 전이 시 base 폴백 (액세서리 자동 사라짐).
- DB 마이그레이션은 기존 `PRAGMA user_version` bump 정책 준수 (진행 데이터 손실 수용).
- 잠금해제 게이트는 본 사이클에서 비도입 — 모든 액세서리 즉시 선택 가능.

## SPEC 분해

### SPEC-001 · DB 스키마 + 마이그레이션
EARS: WHEN 앱이 새 스키마 버전(3)으로 부팅되면 SHALL `pet_state.equipped_accessory` 컬럼이 존재하고 NULL 로 시작한다.

- [x] `PetState.sq` 의 `pet_state` 에 `equipped_accessory TEXT` 컬럼 추가
- [x] `seedDefault` / `selectPet` / `resetProgress` 가 새 컬럼을 처리
- [x] `updateAccessory` 쿼리 신규 추가
- [x] `DriverFactory.SCHEMA_VERSION` 2 → 3 bump

### SPEC-002 · 도메인 모델 + Repository
EARS: WHEN UI 가 액세서리 선택 이벤트를 발행하면 SHALL Repository 가 도메인 객체로 영속한다.

- [x] `domain/model/Accessory.kt` (sealed interface · 5 data object · ALL/fromId)
- [x] `domain/model/Pet.kt` 에 `equippedAccessory: Accessory?` 필드 추가
- [x] `PetRepository.setAccessory(value: Accessory?)` 인터페이스
- [x] `SqlDelightPetRepository` 구현 + `toDomain()` 매핑

### SPEC-003 · UI 이벤트 + 렌더링 분기
EARS: WHEN 펫 상태가 Default 이고 액세서리가 장착되어 있으면 SHALL `pet/idle_<accessoryId>.png` 를 렌더한다. WHILE 다른 상태면 base 스프라이트를 렌더한다.

- [x] `PetUiEvent.SetAccessory(value: Accessory?)` 추가
- [x] `PetStateHolder` 가 SetAccessory 처리 → `repository.setAccessory(...)` 위임
- [x] `rememberPetSprite(state, accessory)` 시그니처 확장 + Default + 액세서리 조합에 한해 액세서리 PNG 우선 로드, 실패 시 base 폴백
- [x] `PetCharacter` / `PetScreen` 이 accessory 파라미터 전파

### SPEC-004 · 트레이 옷장 메뉴
EARS: WHEN 사용자가 트레이 우클릭 → "👕 옷장" 서브메뉴 항목을 선택하면 SHALL 라디오 모델(체크 시에만 set, 해제 콜백 무시) 로 즉시 적용된다.

- [x] `Main.kt` Tray 메뉴에 `Menu("👕 옷장") { CheckboxItem... }` 6 항목(없음 + 5종)
- [x] `PetStrings` 에 라벨 + `accessoryLabel(Accessory)` 매핑 함수

### SPEC-005 · 빌드 검증 + 문서화
EARS: WHEN PR 이 머지되어 release-please 가 minor bump 하면 SHALL 다음 릴리스(`1.3.0`) 가 옷장 기능 포함 상태로 배포된다.

- [x] `./gradlew :composeApp:compileKotlinJvm` 통과
- [ ] **확인 필요**: `./gradlew :composeApp:run` 으로 5종 모두 트레이 토글, 앱 재시작 후 유지
- [x] PRD §11 추가
- [x] 액세서리 PNG 5장 git 추가 (idle_*.png)
- [x] README "주요 기능" 표 갱신

## Out of Scope

- 다중 동시 착용 (β 모델) — 단독 액세서리 PNG 자산화 후 별도 사이클
- 호감도 마일스톤 잠금해제 — 사이클 B
- 9개 상태에 대한 액세서리 자산 (점프/일하는 중 액세서리 유지)
- 펫 우클릭 ContextMenu 에 옷장 추가 — 트레이만 진입점, 메뉴 길이 절제
- 옷장 미리보기 / 호버 임시 적용

## Completion Promise

- [x] 스키마 v3 로 마이그레이션, 새 컬럼 영속
- [x] 트레이 옷장 6 라디오 동작 (코드 구조상 보장, 실행 검증 필요)
- [x] Default 상태에서 액세서리 노출, 다른 상태 전이 시 base 폴백
- [x] `compileKotlinJvm` 통과
- [ ] 실행 검증 + PR 머지 + 1.3.0 릴리스
