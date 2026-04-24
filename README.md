<div align="center">

# Claude Pet 🐣

**Windows / macOS 데스크톱 펫** — Kotlin × Compose Multiplatform

> AI Claude 를 의인화한 작고 수줍은 반려 캐릭터.
> 화면 구석에서 걷고, 점프하고, 배고프면 말을 겁니다.

![platform](https://img.shields.io/badge/platform-macOS%20%7C%20Windows-lightgrey)
![kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin)
![compose](https://img.shields.io/badge/Compose_Multiplatform-1.7-4285F4?logo=jetpackcompose)
![jdk](https://img.shields.io/badge/JDK-21-orange)
![license](https://img.shields.io/badge/license-MIT-green)

<img src="composeApp/src/jvmMain/resources/pet/idle_smile.png" width="140" alt="pet smile"/>
<img src="composeApp/src/jvmMain/resources/pet/idle_jumping.png" width="140" alt="pet jumping"/>
<img src="composeApp/src/jvmMain/resources/pet/idle_hungry.png" width="140" alt="pet hungry"/>

</div>

---

## 한눈에 보기

- **Claude Code 주도 개발 + 하네스 엔지니어링** — 코드는 AI 가 쓰고, 사람은 "AI 가 쓸만한 코드를 생산하게 만드는 환경"(규칙·에이전트·훅·자가 검증)을 설계했습니다.
- **31 개 SPEC** — 요구사항을 EARS 포맷으로 분해, 컴파일 → 개발 run → 번들 실행의 3단 검증을 루프로 돌렸습니다.
- **실전 디버깅** — OpenJDK JIT SIGTRAP, jpackage 모듈 누락, macOS 포그라운드 감지 오매칭 같은 경계 이슈를 추적·해결.
- **태그 한 줄 → 자동 배포** — `git tag vX.Y.Z && git push --tags` 로 macOS `.dmg` / Windows `.msi` 자동 빌드·릴리즈.

---

## 📑 목차

1. [다운로드](#-다운로드)
2. [주요 기능](#-주요-기능)
3. [기술 스택](#-기술-스택)
4. [아키텍처](#️-아키텍처)
5. [AI 협업 방식](#-ai-협업-방식)
6. [실전 디버깅 기록](#-실전-디버깅-기록)
7. [빌드·배포](#-빌드배포)

---

## 📦 다운로드

[Releases](../../releases) 페이지에서 최신 태그를 받으세요. `.dmg`/`.msi` 는 설치 안내(`설치방법.txt`) 와 함께 **zip 번들** 로 배포됩니다.

| OS | 파일 | 설치 |
|---|---|---|
| macOS | `ClaudePet-X.Y.Z-macos.zip` | 압축 풀기 → `.dmg` 더블클릭 → `Applications` 로 드래그 |
| Windows | `ClaudePet-X.Y.Z-windows.zip` | 압축 풀기 → `.msi` 더블클릭 설치 |

<details>
<summary><strong>macOS 보안 경고 해결</strong></summary>

Apple Developer ID 서명을 안 붙여 Gatekeeper 경고가 뜹니다:

```bash
xattr -dr com.apple.quarantine /Applications/ClaudePet.app
open /Applications/ClaudePet.app
```

첫 실행 시 접근성 권한 다이얼로그의 `완료 (재시작)` 버튼이 권한 부여 후 JVM 재시작까지 한 번에 처리합니다.
</details>

<details>
<summary><strong>Windows 에서 "Failed to launch JVM" 이 뜨는 경우</strong></summary>

Authenticode 서명 미적용으로 Windows Defender 가 실행을 차단할 수 있습니다. 해결:

1. 시작 메뉴에서 ClaudePet 우클릭 → **"관리자 권한으로 실행"**
2. 여전히 실패하면 **Windows Defender 예외 목록**에 `C:\Program Files\ClaudePet` 폴더 추가

비개발자용 단계별 스크린 리딩 가이드: [docs/windows-방화벽-허용-가이드.txt](docs/windows-방화벽-허용-가이드.txt)
</details>

---

## 🎮 주요 기능

| 범주 | 동작 |
|---|---|
| **행동** | 10초마다 창 내부 왕복 산책 · 1/6 확률 점프 · 5분 방치 시 지루함 |
| **반응** | 좌클릭 = 미소 · 더블클릭 = 점프 · 드래그 = 위치 저장 · **우클릭 = 메뉴** (밥 주기 / 보이기·숨기기 / 처음부터 시작 / 종료) |
| **생활** | 30초마다 포만감 -1 · 20 이하면 배고픔 · 타이핑이나 트레이·우클릭 🍚 밥주기로 회복 |
| **호감도** | 100 pt / Lv, 5 티어 (존댓말 → 반말 → 애정). 100+ 줄 오리지널 대사, 우클릭 힌트 포함 |
| **작업 감지 (macOS)** | 포그라운드가 IDE · 에디터 · 터미널 · AI 채팅 앱이면 작업 모드. Claude CLI 프로세스도 인식. 화이트리스트: IntelliJ / PyCharm / WebStorm / GoLand / Android Studio / VS Code / Cursor / **Windsurf** / **Zed** / **Fleet** / Xcode / Terminal / iTerm2 / Sublime Text / Claude Desktop / ChatGPT Desktop |
| **작업 감지 (Windows)** | 현재 미구현 — JNA + jpackage 서명 미적용 조합의 Defender 차단 이슈로 롤백, 다음 사이클에서 JDK 22+ FFM 또는 Authenticode 서명 도입 후 재시도 |
| **Claude CLI 감지** | `ProcessHandle` 기반으로 macOS·Windows 공통. 전용 바이너리 / npm global / 쉘 래퍼 모두 감지 (command line 경로 토큰 매칭) |
| **자동 업데이트** | 시작 시 GitHub Releases 조회 → 새 버전 다이얼로그 + 브라우저 연결 |

---

## 🧰 기술 스택

**Kotlin 2.1** · **Compose Multiplatform 1.7** · **JetBrains Runtime 21** · **Koin 4** · **SQLDelight 2** · **kotlinx.coroutines / serialization** · **JNativeHook 2.2** · **jpackage** · **GitHub Actions**

> JBR 21 을 쓰는 이유: Temurin 21.0.10 이 macOS arm64 + Skiko 조합에서 C2 JIT SIGTRAP 으로 죽습니다. JBR 이 AppKit·Skiko 패치를 선반영한 빌드라 안정.

---

## 🏗️ 아키텍처

**Clean + MVI-lite** 3 레이어. Compose Desktop 엔 Android ViewModel 이 없어 Plain Kotlin `PetStateHolder` 가 `StateFlow<UiState>` 를 담당.

```
com.myclaudepet
├── ui/      ← Composable · UiState · UiEvent · StateHolder
├── domain/  ← model · repository(interface) · usecase        (Pure Kotlin)
└── data/    ← SQLDelight · JNativeHook · platform probe · 업데이트
```

- 의존 방향 `ui → domain ← data` 는 `.claude/rules/layer-dependency.md` 로 규칙화되어 있고, 편집 훅이 자동 검증합니다.
- `domain` 은 Kotlin 표준 라이브러리 외 프레임워크 의존 금지 (Compose · Koin · SQLDelight · AWT 전부).
- UseCase 는 단일 `operator fun invoke(...)` — 한 줄 위임이면 만들지 않고 Repository 직접 호출.

---

## 🤖 AI 협업 방식

코드는 Claude Code 가 썼고, 사람은 **AI 가 규칙을 벗어나지 않도록 주변 환경**을 설계했습니다.

### `.claude/` 프로젝트 하네스

```
.claude/
├── CLAUDE.md           세션마다 자동 로드되는 프로젝트 규칙 (스택·네이밍·금지)
├── rules/              레이어 의존 · Kotlin 컨벤션 · Compose UI 규칙
├── agents/             Compose UI · Kotlin 아키 · SQLDelight 전담 서브에이전트
├── skills/rr/          progress.md 체크박스 자동 갱신 슬래시 커맨드
└── hooks/              Kotlin 편집 후 검증 · rm 차단 · 시스템 명령 차단
```

### SPEC 루프

```
요구사항 → 소크라테스식 질문 → PRD(EARS) → SPEC-N 분해
         → 구현 → 3단 검증 → progress.md 체크 → 다음 SPEC
```

### 3단 검증

AI 가 "컴파일 성공 = 동작 성공" 이라 판단하지 않도록:

| 단계 | 방법 | 이 단계가 잡는 것 |
|---|---|---|
| 컴파일 | `./gradlew compileKotlinJvm` | 타입 · 의존성 |
| 개발 run | `./gradlew run` 백그라운드 → PID / stderr 점검 | 런타임 초기화 · JIT 크래시 |
| 번들 실행 | `.dmg` 내부 바이너리 직접 실행 | jlink 모듈 누락 · 번들 리소스 경로 |

자세한 커밋 규칙은 [docs/commit-convention.md](docs/commit-convention.md).

---

## 🔬 실전 디버깅 기록

<details>
<summary><strong>1. Temurin 21.0.10 의 JIT SIGTRAP (exit 133)</strong></summary>

JVM 이 스택트레이스 없이 종료. 터미널에 `Trace/BPT trap: 5` 만 남음. `-Xint` 로 JIT 를 끄면 정상 동작 → JIT 버그 확정. Corretto 21.0.10 도 동일 증상 → OpenJDK 21.0.10 업스트림 문제. JetBrains Runtime 21 로 전환해 해결.
</details>

<details>
<summary><strong>2. jpackage 번들 JRE 의 <code>java.sql</code> 누락</strong></summary>

개발 `run` 은 정상이지만 `.dmg` 로 설치한 앱은 즉시 종료. stderr 에 `NoClassDefFoundError: java/sql/DriverManager`. SQLDelight / sqlite-jdbc 가 JPMS 모듈 선언이 없어 jlink 가 의존을 못 찾음. `nativeDistributions.modules("java.sql", "java.instrument", "jdk.unsupported")` 로 해결.
</details>

<details>
<summary><strong>3. macOS 포그라운드 앱 이름 매칭 실패</strong></summary>

`osascript` 의 실제 반환값 `idea` 와 내가 만든 화이트리스트 `IntelliJ IDEA` 가 `contains` 방향이 반대라 영원히 매칭 안 됨. IDE 포커스해도 Working 전이 안 일어나던 원인. 실제 반환값 기준으로 화이트리스트 재작성.
</details>

---

## 🧑‍💻 빌드·배포

### 로컬 실행

```bash
./gradlew :composeApp:run
```

로컬 JDK 는 **JetBrains Runtime 21 권장**. `~/.gradle/gradle.properties` 에 한 줄:

```properties
org.gradle.java.home=/path/to/jbrsdk-21.0.10.../Contents/Home
```

### 네이티브 패키징

```bash
./gradlew :composeApp:packageDmg   # macOS
./gradlew :composeApp:packageMsi   # Windows (Windows 머신 필요)
```

결과물은 프로젝트 루트 `dist/` 로 자동 복사되고, dmg 는 앱 아이콘 · 볼륨 아이콘 · 볼륨 내부 아이콘까지 자동 주입됩니다.

### 자동 릴리즈

`.github/workflows/release.yml` 가 태그 push 를 감지해 macOS · Windows 러너에서 병렬 빌드 후 **`.dmg`/`.msi` + `설치방법.txt` 를 zip 으로 묶어** GitHub Releases 에 자동 첨부합니다.

```bash
# gradle.properties 의 projectVersion 을 올린 뒤
git tag v1.1.5
git push origin v1.1.5
# → 5~10분 후 Releases 페이지에 ClaudePet-1.1.5-macos.zip / ClaudePet-1.1.5-windows.zip 업로드
```

버전 관리는 [release-please](.github/workflows/release-please.yml) 가 Conventional Commits 기반으로 Release PR 을 자동 생성 / 갱신하며, 해당 PR 이 머지되면 태그가 찍혀 `release.yml` 이 실행됩니다.

---

## 🙏 크레딧

- **원작 영감**: [cchh494/claude-pet](https://github.com/cchh494/claude-pet) (Swift · All Rights Reserved) — 게임 메커닉 / 상태 분류 아이디어만 참고, 이미지 · 대사는 전부 독립 제작
- **이미지**: Google Gemini 생성 + remove.bg 로 배경 제거
- **코드**: [Claude Code](https://claude.com/claude-code)

## 라이선스

MIT — [LICENSE](LICENSE)
