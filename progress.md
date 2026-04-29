# Claude Pet — Progress

> 현재 작업 중인 한 기능 사이클의 SPEC 정의.
> 기능 완료 시 다음 사이클에 맞춰 재작성됨.
> 과거 사이클은 CHANGELOG / git log / PRD 참조.

## 목표

Windows 환경에서 시스템 트레이/메뉴바가 보이지 않거나 발견하기 어려운 경우에도 트레이 핵심 기능(밥 주기·옷장·초기화·종료) 에 접근 가능하도록 펫 우클릭 컨텍스트 메뉴에 동등 기능을 통합한다. macOS 는 메뉴바 트레이가 안정적이므로 우클릭 메뉴는 가벼운 기본 액션만 유지한다.

## 원칙 / 제약

- OS 분기는 단일 진입점(`Main.kt` 의 `isWindows`) 에서 1회 결정해 prop 으로 주입. 컴포넌트 내부 OS 감지 금지.
- Windows 우클릭에서 "숨기기" 는 제거 — 트레이 미가시 환경에서 다시 띄울 길이 사라지는 사고 방지.
- Compose Desktop `ContextMenuArea` 는 서브메뉴/체크박스 미지원 → 옷장은 평면 항목 + ✓ prefix 로 라디오 의미 표현.
- 트레이 자체는 양쪽 OS 모두 그대로 노출 (Windows 환경에서 보이는 경우 중복 접근 허용 → UX 안전망).

## SPEC 분해

### SPEC-001 · 펫 우클릭 메뉴 OS 분기 + Windows 옷장 통합
EARS: WHEN 사용자가 Windows 에서 펫을 우클릭하면 SHALL `밥 주기 / 👕 옷: 없음·5종(현재 장착 ✓) / 처음부터 시작 / 종료` 9 항목 평면 메뉴를 노출한다. WHEN macOS 에서 우클릭하면 SHALL 기존 4 항목(밥 주기 / 보이기·숨기기 / 처음부터 시작 / 종료) 만 노출한다.

- [x] `PetScreen` 에 `isWindows: Boolean = false` 파라미터 추가
- [x] `buildPetContextMenu(...)` private 함수로 메뉴 구성 분리, OS 분기 로직 캡슐화
- [x] `PetStrings.wardrobeContextItem(accessory, equipped)` 헬퍼로 ✓ prefix 라벨 생성
- [x] `Main.kt` 에서 `isWindows = System.getProperty("os.name", "").lowercase().contains("win")` 1회 계산 후 PetScreen 으로 전달 (skiko renderApi 분기에서 이미 계산하던 값 재사용)
- [x] 빌드 검증: `./gradlew :composeApp:compileKotlinJvm` 성공
- [ ] **확인 필요**: macOS `./gradlew :composeApp:run` 으로 우클릭 시 옷장 미노출(기존 4 항목) 확인
- [ ] **확인 필요**: Windows MSI 재빌드 후 우클릭 시 옷장 6항목 + ✓ 마크 정상 노출

## Out of Scope

- 트레이 자체를 Windows 에서 끄는 옵션 — 환경에 따라 트레이가 잘 보이는 경우 중복 접근이 사용자 안전망 역할
- ContextMenuArea 커스텀 Representation 으로 진짜 서브메뉴 구현 — Compose Desktop API 한계로 복잡도 큼, 평면 + ✓ 로 충분
- 항상 위에 두기 토글 / 위치 리셋 / About 다이얼로그 — 별 사이클

## Completion Promise

- [x] OS 분기 단일 진입점에서 결정, 컴포넌트 내부 감지 없음
- [x] Windows 우클릭 메뉴에 옷장 평면 6항목 + 현재 장착 ✓ 표기
- [x] Windows 우클릭에서 "숨기기" 제거 (재노출 불가 사고 방지)
- [x] macOS 우클릭은 기존 4 항목 유지 (트레이 중복 회피)
- [x] README 우클릭/옷장 섹션 OS 별 동작 갱신
- [ ] PR 머지 후 release-please 가 자동 release PR 생성 → 머지 → v1.3.1 자동 빌드/배포
