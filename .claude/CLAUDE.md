# Claude Pet (CMP) — 프로젝트 규칙

> ⚠️ **폴더 이전 진행 중** (이 블록이 있으면 이전 미완료 상태)
> 사용자에게 다른 질문을 받더라도, 먼저 `docs/migration.md` 를 읽고
> 현재 working directory 와 `~/.claude/projects/` 상태를 확인한 뒤
> 남은 Phase 를 안내하세요. 모든 Phase 완료 후 이 블록과 `docs/migration.md` 둘 다 삭제.

Kotlin + Compose Multiplatform 기반 크로스 플랫폼(Windows/macOS) 데스크톱 펫.
모든 작업은 본 문서 + `rules/*.md` + 사용자 글로벌 규칙을 따른다.

---

## 기술 스택 (고정)

- **언어**: Kotlin 2.1.x
- **UI**: Compose Multiplatform 1.7.x (Desktop target)
- **JVM**: 21 (LTS)
- **빌드**: Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`)
- **DI**: Koin 4.x (startKoin at `main`)
- **DB**: SQLDelight 2.x (SQLite)
- **영속 key-value**: DataStore (Preferences)
- **글로벌 키 후킹**: JNativeHook (Win/Mac 공용)
- **동시성**: kotlinx.coroutines (`CoroutineScope(SupervisorJob() + Dispatchers.Default)`)
- **단방향 상태**: `StateFlow<UiState>` + `sealed interface UiEvent`

> 버전 변경 시 반드시 `gradle/libs.versions.toml` 한 곳에서만 수정. 하드코딩 금지.

---

## 아키텍처 (Clean + MVI-lite)

```
com.myclaudepet
├── ui/        ← Composable, UiState, UiEvent (프레젠테이션)
├── domain/    ← Pure Kotlin. model / repository(interface) / usecase
└── data/      ← SQLDelight, JNativeHook, DataStore 구현체
```

의존성 방향: `ui → domain ← data`. 역방향 import 금지.
`domain`은 Compose/JetBrains/Android 의존성 전면 금지 (Pure Kotlin only).

---

## 네이밍 규칙

| 대상 | 규칙 | 예 |
|---|---|---|
| 패키지 | 소문자, 단수 | `ui.pet`, `domain.model` |
| Composable 함수 | PascalCase, 명사구 | `PetCharacter`, `AffinityBar` |
| State | `...UiState` suffix | `PetUiState` |
| Event/Intent | `...UiEvent` suffix | `PetUiEvent.Clicked` |
| UseCase | 동사구 + UseCase | `ObservePetUseCase`, `FeedPetUseCase` |
| Repository 인터페이스 | 명사 + Repository | `PetRepository` |
| Repository 구현 | `...RepositoryImpl` | `SqlDelightPetRepository` (구현 기술 prefix 허용) |
| Flow property | 명사 + `Flow`는 붙이지 않음. `petState: StateFlow<Pet>` | |

---

## Compose 규칙

- 모든 Composable은 `Modifier` 파라미터를 **첫 번째 선택 파라미터로** 받고 기본값 `Modifier`.
- `remember` 없이 state hoisting이 가능한 구조를 먼저 고려.
- 색상·치수는 `ui.theme.PetTheme` 경유. 리터럴(`Color(0xFF...)`, `16.dp`)을 Composable 본문에 흩뿌리지 않는다.
- 애니메이션은 `animateFloatAsState` / `Transition` 우선, Lottie는 캐릭터 전용.

---

## 코드 품질

- 함수 30줄, 파일 300줄 초과 시 분리 고려.
- 주석은 **왜**만. 무엇은 네이밍으로.
- 예외는 도메인 경계에서만 처리, UI에서 `runCatching` 남발 금지.
- 불필요한 추상화(인터페이스 1구현 + 의미 없음) 금지.

---

## 작업 흐름

1. 새 기능 → `docs/prd.md` 업데이트 → SPEC 추가 → `progress.md` 갱신.
2. SPEC 단위로 구현 → `./gradlew :composeApp:compileKotlinJvm` 로컬 검증.
3. UI 변경 시 `./gradlew :composeApp:run` 로 실제 실행 확인.
4. `progress.md`의 체크박스를 완료 즉시 체크.

---

## 금지 사항

- `Co-Authored-By` 커밋 메시지 포함 금지 (사용자 전역 규칙).
- `!!` (non-null assertion) 남발 금지. 불가피하면 주석으로 **왜**.
- `GlobalScope` 금지. 항상 주입된 `CoroutineScope` 사용.
- `println` 로깅 금지. `kotlin-logging` 또는 최소 `java.util.logging.Logger` 사용.
- `var`을 상태 보관 목적으로 사용 금지. `StateFlow`/`mutableStateOf` 중 하나.

---

## 참고

- 글로벌 사용자 규칙: `~/.claude/CLAUDE.md`
- 리뷰 체크리스트: `~/.claude/rules/review-checklist.md`
- 진행 현황: `./progress.md`
- PRD: `./docs/prd.md`
