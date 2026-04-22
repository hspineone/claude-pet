---
name: go
description: claude-pet 전용 progress.md 관리 스킬. 현재 한 기능 사이클의 SPEC 정의만 유지하며, 기능 완료 시 다음 사이클로 전체 재작성한다. Phase 누적 금지.
disable-model-invocation: true
---

# /go — Cycle-scoped Progress

`progress.md` 를 **한 기능 사이클 단위 리빙 도큐먼트**로 관리한다.
글로벌 `/rr` 과 달리 과거 사이클을 누적하지 않고, 기능이 바뀔 때마다 문서를 재작성한다.

## 전제

- `progress.md` 는 "지금 진행 중인 한 기능" 의 SPEC 스냅샷이다.
- 완료된 사이클은 CHANGELOG / git log / PRD 에 이미 남아 있으므로 progress.md 에는 보존하지 않는다.
- SPEC 번호는 프로젝트 누적이 아니라 **사이클 내 로컬 번호(SPEC-001 ~ SPEC-N)** 로 매번 재시작한다.
- Phase 개념을 쓰지 않는다. PR 제목·본문·커밋 메시지 어디에도 "Phase N" 지칭 금지.

## 3가지 동작 모드

### 1) 진행 중 사이클 갱신 (기본)

사용자가 작업 완료/추가를 알리면 **현재 사이클** 문서만 수정한다.

1. `progress.md` 를 읽는다.
2. 해당 SPEC 섹션의 체크박스 `[ ]` → `[x]`.
3. 새 작업이 파악되면 현재 사이클에 SPEC 추가 (EARS 포맷, 다음 로컬 번호 부여).
4. Out of Scope 에 있던 항목이 사이클 안으로 들어왔으면 Out of Scope 에서 제거하고 SPEC 으로 승격.
5. 변경 요약 1-2줄 보고.

### 2) 사이클 완료 감지 → 다음 기능 확인

Completion Promise 의 모든 체크박스가 `[x]` 면 현재 사이클 완료로 판단한다.

1. 사용자에게 "현재 사이클 완료 상태입니다. 다음 기능을 착수하시겠어요? 착수하시면 progress.md 를 새 사이클로 재작성합니다." 로 알린다.
2. 사용자가 다음 기능 요약을 주기 전까지 재작성 금지.
3. 답을 모르면 소크라테스식 질문으로 새 기능의 목표·원칙·SPEC 을 유도한다.

### 3) 새 사이클 재작성

사용자가 새 기능을 착수하겠다고 하면 `progress.md` 를 **전체 교체**한다.

아래 템플릿을 사용한다.

```markdown
# Claude Pet — Progress

> 현재 작업 중인 한 기능 사이클의 SPEC 정의.
> 기능 완료 시 다음 사이클에 맞춰 재작성됨.
> 과거 사이클은 CHANGELOG / git log / PRD 참조.

## 목표

<한 줄 요약 — 이 사이클이 달성하려는 것>

관련 PRD: §N (FR-xx, FR-yy)

## 원칙 / 제약

- <근본 원칙 1>
- <근본 원칙 2>
- <기술적 제약>

## SPEC 분해

### SPEC-001 · <요약>
EARS: WHEN ... / IF ... / SHALL ...

- [ ] 구현 체크박스들
- [ ] 빌드 검증: `./gradlew :composeApp:compileKotlinJvm`
- [ ] 실행 검증: 구체 조건

### SPEC-002 · <요약>
...

## Out of Scope

- <이 사이클에서 의도적으로 뺀 것>

## Completion Promise

- [ ] <사이클 완료 기준 1>
- [ ] <사이클 완료 기준 2>
```

## 규칙

- 체크 안 된 SPEC 을 "완료됐을 것으로 보입니다" 같이 추측 금지. 사용자 확인.
- 모든 SPEC 은 EARS 포맷(`WHEN / IF / SHALL / WHILE`) 포함.
- SPEC 번호는 사이클 내에서 재사용 금지 (삭제해도 번호 유지).
- 재작성 시 이전 사이클 내용은 파일에서 완전 제거. 백업/주석 처리 금지 — 히스토리는 git 에 있음.
- PRD(`docs/prd.md`) 는 누적 문서라 이 스킬의 재작성 대상이 아니다. 혼동 금지.
