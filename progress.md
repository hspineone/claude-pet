# Claude Pet — Progress

> 현재 작업 중인 한 기능 사이클의 SPEC 정의.
> 기능 완료 시 다음 사이클에 맞춰 재작성됨.
> 과거 사이클은 CHANGELOG / git log / PRD 참조.

## 목표

말풍선 대사 길이와 무관하게 포만감/친밀도(Stats) UI 가 항상 윈도우 하단에 노출되도록 레이아웃을 분리한다. 우클릭 힌트 등 멀티라인 대사로 인해 Column 합계 높이가 윈도우(260dp) 를 초과하면서 stats 영역이 클립되던 현상을 제거한다.

## 원칙 / 제약

- Stats 노출은 다른 어떤 UI 요소(말풍선·펫 애니메이션) 의 길이/오프셋과 독립이어야 한다.
- 윈도우 크기(260x260) 는 변경하지 않는다 — 변경 시 위치 저장 마이그레이션 필요.
- 말풍선이 매우 길어 영역을 넘는 케이스의 우선순위는 stats 보존 > 말풍선 전체 표시.
- 토큰화: stats 영역 높이를 `PetDimens` 상수로 격리, 매직 넘버 금지.

## SPEC 분해

### SPEC-001 · Stats 영역과 말풍선/펫 영역 레이아웃 분리
EARS: WHEN 말풍선 텍스트 길이가 어떤 값이든 SHALL StatsOverlay 는 윈도우 하단 padding 4dp 위치에 항상 렌더링된다. WHILE 말풍선이 멀티라인으로 자라도 stats 좌표는 변하지 않는다.

- [x] `ui/pet/PetScreen.kt` 의 단일 Column 구조를 두 자식(Box align)으로 분해
  - 펫·말풍선 Column: `align(BottomCenter)` + `padding(bottom = StatsAreaHeight + 8.dp)` 로 stats 영역 위쪽에 격리
  - StatsOverlay: 동일 Box 의 별도 `align(BottomCenter)` + `padding(bottom = 4.dp)` 자식
- [x] `ui/theme/PetDimens.kt` 에 `StatsAreaHeight = 50.dp` 토큰 추가 (구성요소 합산 근거 주석 포함)
- [x] 사용하지 않게 된 `Spacer` / `height` import 제거
- [x] 빌드 검증: `./gradlew :composeApp:compileKotlinJvm` 성공
- [ ] **확인 필요**: macOS 실행 검증 (`./gradlew :composeApp:run`) — 우클릭 힌트 대사 출력 시 stats 유지

### SPEC-002 · 버전 bump 및 배포
EARS: WHEN PR 이 머지되면 SHALL release-please 가 patch bump (1.1.6 → 1.1.7) 후 GitHub Releases 에 태그를 게시하고, 다음 실행에서 업데이트 다이얼로그가 1.1.7 을 표기한다.

- [ ] PR 생성 후 사용자 머지
- [ ] release-please 자동 태깅 / 릴리스 노트 게시
- [ ] 다음 기동 시 UpdateDialog 노출 — **확인 필요**

## Out of Scope

- 윈도우 높이 증가 또는 BubbleMaxWidth 축소를 통한 "긴 말풍선 클립 방지" — 별 사이클로 분리
- 말풍선 자체 스크롤/요약/말줄임 처리
- Stats UI 디자인 변경(색상·바 높이·폰트)
- README 갱신 — stats UI 노출은 사용자 관점 동작 차이 없음(원래 보여야 했던 게 보일 뿐), 기능 변경 아님

## Completion Promise

- [x] PetScreen 의 Column 합계가 윈도우 높이를 초과해도 StatsOverlay 좌표가 변하지 않음 (코드 구조상 보장)
- [x] `compileKotlinJvm` 통과
- [ ] 우클릭 힌트 대사(2~3줄) 시 stats 유지 — 실행 검증 필요
- [ ] PR 머지 후 1.1.7 릴리스 노출
