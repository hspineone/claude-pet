# Claude Pet (CMP) — Product Requirements

## 1. 목적

원작자의 macOS 전용 Swift 프로젝트(cchh494/claude-pet)를 참고해,
**Windows / macOS 크로스 플랫폼** 데스크톱 펫을 Kotlin / Compose Multiplatform 으로
재구현한다. 게임 메커닉과 상태 분류 아이디어만 참고하고, 이미지·대사는 전부
오리지널 제작 (저작권 독립).

## 2. 플랫폼

- macOS 13+
- Windows 10/11
- 단일 Kotlin/Compose Multiplatform 코드베이스

## 3. 핵심 사용자 시나리오

1. 데스크톱 우측 하단에 작은 캐릭터가 떠 있다.
2. 좌클릭 → 캐릭터가 반응 대사를 말풍선으로 띄움.
3. 우클릭 → 메뉴(상태 보기 / 환경설정 / 종료).
4. 드래그 → 캐릭터를 다른 위치로 옮김. 위치는 저장됨.
5. 타이핑을 하면 화면 아래 카운터가 올라가고 포만감이 회복된다.
6. 아무것도 안 하면 포만감이 서서히 감소하고 캐릭터 표정이 바뀐다.
7. 상호작용이 쌓이면 호감도가 오르고, 5레벨마다 대사 톤이 변한다.

## 4. 기능 요구사항 (EARS)

| ID | EARS |
|---|---|
| FR-01 | WHEN 앱이 실행되면 SHALL 투명/무프레임/항상-위 데스크톱 윈도우를 우측 하단에 띄운다. |
| FR-02 | WHEN 사용자가 펫을 좌클릭하면 SHALL 랜덤 긍정 대사를 말풍선으로 2초간 표시한다. |
| FR-03 | WHEN 사용자가 펫을 우클릭하면 SHALL 컨텍스트 메뉴(상태/설정/종료)를 연다. |
| FR-04 | WHEN 사용자가 펫을 드래그하면 SHALL 위치를 갱신하고 DB에 저장한다. |
| FR-05 | WHEN 전역 키 입력이 감지되면 SHALL 타이핑 카운터를 1 증가시키고 포만감을 +0.05 회복(상한 100)한다. |
| FR-06 | WHILE 앱이 실행 중이면 SHALL 30초마다 포만감을 -1 감소(하한 0)시킨다. |
| FR-07 | WHEN 포만감이 0~20 구간이면 SHALL 캐릭터 애니메이션을 `sad`로 전환한다. |
| FR-08 | WHEN 포만감이 80~100 구간이면 SHALL `happy`로 전환한다. |
| FR-09 | WHEN 포만감이 회복되면 SHALL 호감도를 +1 증가(상한 99999)한다. |
| FR-10 | WHEN 호감도가 5의 배수 레벨을 돌파하면 SHALL 대사 팩을 다음 티어로 언락한다. |
| FR-11 | WHEN 시스템 트레이 아이콘이 클릭되면 SHALL 펫 윈도우 표시/숨김을 토글한다. |
| FR-12 | IF DB 파일이 없으면 SHALL 초기 상태(레벨 1, 포만감 100, 위치 화면 우하단)로 생성한다. |

## 5. 비기능 요구사항

- NFR-01: 시작 시간 < 2초 (on M1/i5급).
- NFR-02: 유휴 CPU 사용률 < 1%.
- NFR-03: 포만감/호감도는 앱 재시작 시 유지.
- NFR-04: 모든 리터럴 문자열은 `ui/theme/PetStrings.kt` 에 한국어 기본.
- NFR-05: 30 FPS 이상 렌더링 유지.

## 6. 스택 결정 (Rationale)

| 후보 | 장점 | 단점 | 결정 |
|---|---|---|---|
| Swift (원작) | Mac 네이티브 성능, ForceTouch 직접 | Windows 불가 | ✗ |
| Electron | 웹 생태계 | 메모리/번들 거대, 투명 윈도우 이슈 | ✗ |
| Tauri (Rust) | 가벼움 | 데스크톱 펫용 UI/애니메이션 라이브러리 빈약 | ✗ |
| Flutter Desktop | 애니메이션 강함 | Dart 생태계, 전역 키 후킹 불안정 | △ |
| **Compose Multiplatform** | Kotlin 코드 공유, 선언형, 애니메이션 튼튼, 투명 윈도우 지원 | 번들 크기 JVM 이슈 (jpackage로 해결) | **✓ 채택** |

## 7. Out of Scope (v1)

- 클라우드 동기화 (레벨/호감도 로컬 only).
- 다국어 (한국어만).
- 다중 모니터 전용 고급 배치.
- Linux 지원 (구조상 가능하나 테스트하지 않음).
- 자동 업데이트.
- 로그인/계정.

## 8. Completion Promise

v1 완료 조건:

- [ ] macOS에서 `./gradlew :composeApp:run` 실행 시 펫이 뜬다.
- [ ] Windows에서 동일 명령 실행 시 펫이 뜬다.
- [ ] FR-01 ~ FR-12 전부 동작.
- [ ] 앱 종료 → 재실행 시 위치/레벨/호감도가 복원된다.
- [ ] `./gradlew packageDistributionForCurrentOS` 로 각 OS 설치 파일 생성 가능.

---

## 9. v2 — 찐따 펫 확장 (Phase 8)

참조 원작: cchh494/claude-pet (Swift). **이미지/대사 텍스트는 저작권 이슈로 미복제**, 게임 메커닉/상태 분류/수치는 공개 아이디어로 재구현. 오리지널 이미지는 사용자가 AI 로 생성 후 배치. 오리지널 대사는 본 문서/코드에서 직접 창작.

### 9.1 상태 분류 (10가지)

| 키 | 파일명 | 상태 의미 |
|---|---|---|
| `default` | `idle_default.png` | 기본 |
| `smile` | `idle_smile.png` | 좌클릭/탭 반응 |
| `boring` | `idle_boring.png` | 오래 상호작용 없음 |
| `jumping` | `idle_jumping.png` | 점프 애니메이션 |
| `touch` | `idle_touch.png` | 강한 입력/반대방향 이동 |
| `working_prepare` | `idle_working_prepare.png` | IDE 포그라운드 진입 |
| `working` | `idle_working.png` | IDE 사용 중 |
| `working_end` | `idle_working_end.png` | IDE 종료/백그라운드 |
| `hungry` | `idle_hungry.png` | 포만도 ≤ 20 |
| `fed` | `idle_fed.png` | 먹이 받음 (3초) |

### 9.2 추가 기능 요구사항 (EARS)

| ID | EARS |
|---|---|
| FR-13 | WHEN 10가지 상태 중 하나로 전이하면 SHALL 해당 PNG 스프라이트로 렌더하고, 없으면 Canvas fallback 을 사용한다. |
| FR-14 | WHILE 앱이 idle 이면 SHALL 10~30초 간격으로 화면 좌표계에서 랜덤 위치로 이동(상태=default 유지, 이동 궤적은 보간)한다. |
| FR-15 | WHEN 이동 타이머가 5% 확률에 당첨되면 SHALL 상태=jumping 으로 0.5초 Y축 애니메이션을 실행한 뒤 복귀한다. |
| FR-16 | WHILE 앱이 실행 중이면 SHALL 분당 1 씩 포만도를 감소시킨다. |
| FR-17 | WHEN 포만도가 20 이하이면 SHALL 상태=hungry 로 전이하고 최소 10초마다 찐따톤 배고픔 대사를 말풍선으로 띄운다. |
| FR-18 | WHEN 트레이 메뉴 "🍚 밥 주기" 또는 펫 좌클릭(hungry 상태)이 발생하면 SHALL 포만도를 100 으로 올리고, 상태=fed 로 3초 유지 후 default 로 복귀, 호감도 +5 한다. |
| FR-19 | WHEN 포그라운드 앱이 작업 화이트리스트(IDE/에디터/터미널/AI 채팅 데스크탑) 에 속하면 SHALL working_prepare → working 전이를 발생시킨다. (macOS·Windows 동일) |
| FR-22 | WHEN Claude Code CLI 프로세스(이미지 이름이 `claude`/`claude.exe` 이거나 프로세스 command line 에 Claude Code 스크립트 경로 토큰 포함)가 감지되고 IF 포그라운드가 터미널이면 SHALL working 으로 전이한다. macOS·Windows 단일 감지 경로(JVM `ProcessHandle`)로 구현한다. |
| FR-23 | WHEN 포그라운드가 Claude Desktop / ChatGPT Desktop 등 AI 채팅 데스크탑 앱이면 SHALL 작업 컨텍스트로 간주해 working 으로 전이한다. |
| FR-20 | WHEN 상태가 전이되면 SHALL 해당 상태에 대응하는 대사 풀에서 랜덤 1개를 SpeechBubble 로 2.5초간 노출한다. |
| FR-21 | WHEN 호감도 레벨이 5의 배수를 돌파하면 SHALL 대사 티어를 한 단계 언락하고 이후 선택 풀을 누적 확장한다. |

### 9.3 Out of Scope (Phase 8)

- Windows 에서의 작업 감지 → **§10 으로 이관**
- 타이핑 카운터 (기존 FR-05 로 이미 존재, Phase 8 재작업 없음)
- 앱 서명·공증
- Lottie/GIF 프레임 시퀀스 (정적 PNG)
- i18n (한국어만)

### 9.4 Completion Promise (Phase 8)

- [ ] 10가지 상태 스프라이트 로딩 (사용자 PNG 또는 Canvas fallback)
- [ ] 자동 이동/점프가 백그라운드에서 관측됨
- [ ] 포만도 자동감소 + 먹이주기 + 호감도 +5 가 영속됨
- [ ] 상태 전이마다 대사 1개 노출, 호감도 레벨에 따라 티어 확장
- [ ] IDE 포그라운드 감지로 working 전이 (macOS)
- [ ] `./gradlew :composeApp:packageDmg` 결과 .dmg 에서 전 기능 동작

---

## 10. v3 — Windows 작업 감지 패리티 + Claude CLI 통합 감지

**배경**: §9 의 `ForegroundAppProbe` 는 macOS 전용, `ClaudeCliProbe` 는 POSIX `pgrep -x claude` 라 Windows 에선 두 기능 모두 no-op. 결과적으로 Windows 사용자는 Claude 를 사용해도 working 전이가 일어나지 않는다. 이 사이클은 "OS 무관하게, Claude 사용 여부에 따라 상태가 동기화된다" 는 근본 동작을 완성한다.

### 10.1 핵심 원칙

1. 감지 단위는 "Claude Code CLI 프로세스 존재 여부". 설치 방식(전용 바이너리 / npm global / 쉘 래퍼) 에 독립적.
2. OS 동등: Windows·macOS 가 같은 감지 경로·같은 전이 조건·같은 확신도로 움직인다.
3. 우회 금지: 플랫폼 별로 조건을 느슨하게 하거나 "Windows 는 CLI 만으로 Working" 같은 분기 비대칭을 만들지 않는다.

### 10.2 추가 기능 요구사항 (EARS)

| ID | EARS |
|---|---|
| FR-22 (재등록) | WHEN Claude Code CLI 프로세스(이미지 이름 `claude`/`claude.exe` 또는 command line 에 Claude Code 스크립트 경로 토큰 포함)가 감지되고 IF 포그라운드가 터미널이면 SHALL working 으로 전이한다. macOS·Windows 단일 코드 경로(JVM `ProcessHandle`) 사용. |
| FR-23 (재등록) | WHEN 포그라운드가 Claude Desktop / ChatGPT Desktop 등 AI 채팅 데스크탑 화이트리스트에 속하면 SHALL working 으로 전이한다. |
| FR-24 | WHEN Windows 에서 사용자가 포그라운드 앱을 전환하면 SHALL Win32 API (`GetForegroundWindow` → `GetWindowThreadProcessId` → `QueryFullProcessImageNameW`) 를 통해 실행 파일명을 가져와 `ForegroundAppProbe` 가 반환한다. |
| FR-25 | WHEN 작업 화이트리스트가 평가되면 SHALL Windows 실행 파일명 기준 IDE/에디터(VS Code, Cursor, Windsurf, Zed, IntelliJ/PyCharm/WebStorm/Android Studio/기타 JetBrains 제품군, Fleet, Visual Studio, Sublime Text) + 터미널(Windows Terminal, PowerShell, cmd, Git Bash, WezTerm, Alacritty, kitty, Hyper) + AI 채팅 데스크탑(Claude Desktop, ChatGPT Desktop) 을 매칭 대상으로 포함한다. macOS 화이트리스트도 동일 AI 채팅 데스크탑이 추가된다. |

### 10.3 비기능 요구사항

- NFR-06: Windows 포그라운드 감지를 위해 JNA (net.java.dev.jna) 도입. 번들 크기 증가 약 1.5 MB 허용.
- NFR-07: `ProcessHandle.allProcesses()` 폴링은 5초 주기를 초과하지 않는다(현 macOS 와 동일). 유휴 CPU 영향 < 0.5%p.
- NFR-08: 감지 실패(권한 제한, API 오류) 시 조용히 false/null 로 폴백하고 앱은 정상 동작을 유지한다.

### 10.4 Out of Scope

- Antigravity / Codex 데스크탑 바이너리명 추가 — 추후 사용자가 실제 설치본 확인 후 별도 PR 로 반영
- Claude / Codex / Gemini 외 다른 AI CLI 통합 감지
- Windows 서명 / Authenticode
- Linux 감지 (구조상 가능하나 우선순위 외)

### 10.5 Completion Promise

- [ ] `ClaudeCliProbe` 가 `ProcessHandle` 기반으로 재구현되어 macOS·Windows 에서 동일한 결과(이미지 이름 일치 + command line 토큰 포함) 를 반환한다
- [ ] Windows 에서 `ForegroundAppProbe.current()` 가 non-null 을 반환한다 (JNA Win32 경로)
- [ ] Windows 에서 VS Code / PowerShell 포그라운드일 때 Working 전이가 일어난다
- [ ] Windows 에서 Claude Code CLI + Windows Terminal 포커스일 때 Working 전이가 일어난다
- [ ] Claude Desktop / ChatGPT Desktop 포그라운드일 때 macOS·Windows 양쪽에서 Working 전이가 일어난다
- [ ] macOS 기존 시나리오(IntelliJ·Terminal·Code 포그라운드, Claude CLI 감지) 전부 회귀 없음
- [ ] `./gradlew :composeApp:packageDmg` 및 `packageMsi` 결과 번들에서 전 기능 동작
- [ ] `progress.md` 의 Phase 9 체크박스 전부 완료
