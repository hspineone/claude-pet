# 폴더 이전 가이드 (claude-pet)

이 파일이 **존재한다는 사실 자체가 이전 작업이 아직 완료되지 않았다**는 신호입니다.
Phase 6 까지 마치면 이 파일을 삭제합니다.

---

## 이전 전/후 경로

| 항목 | 기존(OLD) | 신규(NEW) |
|---|---|---|
| 프로젝트 | `/Users/hyeonseok/Desktop/cluade-pet` | `/Users/hyeonseok/Documents/personal/claude-pet` |
| Claude Code 메모리·세션 | `~/.claude/projects/-Users-hyeonseok-Desktop-cluade-pet` | `~/.claude/projects/-Users-hyeonseok-Documents-personal-claude-pet` |

> Claude Code 는 프로젝트 경로의 `/` 를 `-` 로 치환한 이름으로 `~/.claude/projects/` 하위 폴더를 만듭니다. 그래서 프로젝트를 옮기면 메모리·세션 폴더도 같은 규칙으로 이름을 바꿔 이동해야 이어집니다.

## 이전 이유

- 폴더명 오타 정정 (`cluade` → `claude`)
- 개인 프로젝트 디렉토리 구조화 (`~/Documents/personal/` 아래로)

---

## Claude 새 세션을 읽고 있다면

이 파일을 읽는 Claude 는 이미 사용자가 **새 경로에서 세션을 재시작한 상태**일 가능성이 높습니다. 다음 순서로 지원해주세요:

1. 현재 working directory 가 `/Users/hyeonseok/Documents/personal/claude-pet` 인지 `pwd` 로 확인
2. `~/.claude/projects/` 에 구·신 두 폴더 중 무엇이 남아있는지 확인
3. 아래 "실행 체크리스트"에서 남은 Phase 만 이어서 실행

---

## 실행 체크리스트

### Phase 1 — 사전 정리 (옛 경로에서 수행)

- [ ] `git status` 가 clean 인지 확인
- [ ] 최신 커밋 push 완료 (`git push origin main`)
- [ ] Gradle 데몬 중단
  ```
  cd /Users/hyeonseok/Desktop/cluade-pet && ./gradlew --stop
  ```
- [ ] IntelliJ 에서 해당 프로젝트 창 닫기 (`File → Close Project`)

### Phase 2 — 프로젝트 폴더 이동

```
mkdir -p /Users/hyeonseok/Documents/personal
mv /Users/hyeonseok/Desktop/cluade-pet /Users/hyeonseok/Documents/personal/claude-pet
```

> 새 경로에 문자열 `pineone` 이 포함되면 `~/.gitconfig` 의 `includeIf` 가 잡아채서 회사 identity 로 오작동합니다. `/personal/` 같이 포함되지 않는 경로만 사용.

### Phase 3 — Claude Code 메모리·세션 폴더 이동

```
mv ~/.claude/projects/-Users-hyeonseok-Desktop-cluade-pet \
   ~/.claude/projects/-Users-hyeonseok-Documents-personal-claude-pet
```

이 한 줄로 `memory/`, 각 세션의 `*.jsonl`, `todos/` 등 하위 파일 전부 같이 이동됩니다.

### Phase 4 — 검증 (새 경로에서)

```
cd /Users/hyeonseok/Documents/personal/claude-pet
git remote -v                                          # SSH URL 그대로인지
git ls-remote origin HEAD                              # 개인 키(HyeonSeok7) 인증 성공인지
./gradlew :composeApp:compileKotlinJvm                 # 빌드 성공인지
```

세 명령이 에러 없이 끝나면 프로젝트 측은 정상입니다.

### Phase 5 — IntelliJ 설정

- `File → Open` 으로 **새 경로** (`~/Documents/personal/claude-pet`) 프로젝트 열기
- 기존 Desktop 경로 프로젝트는 Recent Projects 에서 `Remove from Recent` (빈 폴더를 가리키게 됨)
- `Project Structure → SDKs` 에서 JBR 21 경로가 유효한지 확인
- 한 번 `./gradlew --stop` 후 Gradle Sync 다시 돌리기 (캐시 경로 갱신)

### Phase 6 — 마무리 정리

- [ ] `.claude/CLAUDE.md` 최상단의 "폴더 이전 진행 중" 블록 삭제
- [ ] 이 파일(`docs/migration.md`) 삭제
- [ ] 정리 커밋:
  ```
  git add -A
  git commit -m "chore(docs): 폴더 이전 완료 — migration 가이드 제거"
  git push origin main
  ```

---

## 이전 영향 없는 것들 (참고)

| 항목 | 이유 |
|---|---|
| git remote (SSH URL) | `git@github.com:HyeonSeok7/claude-pet.git` — 로컬 경로 무관 |
| `~/.gitconfig*` / SSH 키 / `~/.gradle/gradle.properties` | 전부 home 기준 |
| 프로젝트 내부 `.git/`, `build/`, `.gradle/` | 폴더 내부라 `mv` 로 같이 이동 |

## 주의 사항

- **Phase 3 를 Phase 2 와 함께, 새 세션 열기 전에** 완료해주세요. 메모리 폴더가 이전 안 된 상태로 새 경로에서 세션을 열면 Claude 가 빈 메모리로 시작됩니다.
- Phase 6 전까지는 `docs/migration.md` 를 지우지 마세요. 중간에 문제 생기면 이 파일이 복구의 유일한 지침입니다.
