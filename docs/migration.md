# 폴더 이전 인수인계 (claude-pet)

이 파일이 **존재한다는 사실 자체가 이전 뒷정리가 아직 끝나지 않았다**는 신호입니다.
남은 Phase 4~6 를 마치면 이 파일을 삭제합니다.

---

## 실제 이전 결과 (이미 완료된 것)

| 항목 | 최종 경로 |
|---|---|
| 프로젝트 | `/Users/hyeonseok/Desktop/personal/claude-pet` (454MB) |
| Claude Code 메모리·세션 | `~/.claude/projects/-Users-hyeonseok-Desktop-personal-claude-pet` (11MB) |

정리된 것(휴지통에 있음, 필요시 복구 가능):
- 옛 유령 `~/Desktop/cluade-pet` → `~/.Trash/cluade-pet.ghost.*`
- 중간 경유지 `~/Documents/personal` → `~/.Trash/Documents_personal.*`

---

## 새 세션 Claude 에게 — 남은 Phase

### Phase 4 — 검증

아래 세 명령이 전부 에러 없이 끝나는지 확인:

```
cd /Users/hyeonseok/Desktop/personal/claude-pet
git remote -v                                 # SSH URL (git@github.com:HyeonSeok7/claude-pet.git)
git ls-remote origin HEAD                     # personal SSH 키 인증 성공
./gradlew :composeApp:compileKotlinJvm        # 빌드 성공
```

### Phase 5 — 고아 메모리 폴더 정리

이전 세션이 끝날 때까지 Claude Code 가 옛 이름 폴더에 세션 로그를 계속 기록하고 있었습니다. 남은 `.jsonl` 이 있으면 새 폴더로 합쳐주고 옛 폴더는 휴지통으로:

```
ls ~/.claude/projects/-Users-hyeonseok-Desktop-cluade-pet 2>/dev/null && \
  mv ~/.claude/projects/-Users-hyeonseok-Desktop-cluade-pet/*.jsonl \
     ~/.claude/projects/-Users-hyeonseok-Desktop-personal-claude-pet/ 2>/dev/null; \
mv ~/.claude/projects/-Users-hyeonseok-Desktop-cluade-pet ~/.Trash/claude-memory-old.$(date +%s) 2>/dev/null
```

(폴더가 이미 없으면 명령이 "No such file" 로 끝나도 정상 — 할 일이 없는 것.)

### Phase 6 — 마무리 커밋

- `.claude/CLAUDE.md` 최상단의 "폴더 이전 진행 중" 경고 블록 삭제
- 이 파일 `docs/migration.md` 삭제
- 정리 커밋:
  ```
  git add -A
  git commit -m "chore(docs): 폴더 이전 완료 — migration 가이드 제거"
  git push origin main
  ```

---

## 참고 — 이전 영향 없는 것들

| 항목 | 이유 |
|---|---|
| git remote (SSH URL) | `git@github.com:HyeonSeok7/claude-pet.git` — 로컬 경로 무관 |
| `~/.gitconfig*` / SSH 키 / `~/.gradle/gradle.properties` | 전부 home 기준 |
| 프로젝트 내부 `.git/`, `build/`, `.gradle/` | 폴더 내부라 같이 이동됨 |

## 주의

- 새 경로 `~/Desktop/personal/claude-pet` 에는 문자열 "pineone" 이 없으므로 `includeIf` 가 발동하지 않아 개인 identity + 개인 SSH 키가 정상 적용됩니다.
- Phase 6 전까지 이 파일을 지우지 마세요. 중간 문제 발생 시 복구의 유일한 지침입니다.
