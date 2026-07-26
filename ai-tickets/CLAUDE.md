# ai-tickets

이 폴더는 AI(Claude)가 수행할 작업 티켓 목록이다.

---

## 폴더 구조

```
ai-tickets/
├── todo/       # 아직 시작하지 않은 티켓 · 티켓의 미착수 phase
├── doing/      # 진행 중인 티켓의 overview.md + 지금 수행 중인 phase 1개
└── done/       # 완료된 phase · 완료된 티켓
```

**상태는 phase(페이즈) 단위로 폴더 위치로 표현한다.** 따라서 한 티켓의 파일이 상태에 따라
세 폴더에 나뉘어 존재할 수 있다(같은 이름의 티켓 폴더가 여러 상태 폴더에 동시에 있을 수 있음).

- **미착수 티켓**: `overview.md`와 모든 phase가 `todo/{ticket}/`에 통째로 있다.
- **진행 중 티켓**: `doing/{ticket}/`에 `overview.md` + **지금 하는 phase 1개**만.
  완료한 phase는 `done/{ticket}/`으로, 아직 안 한 phase는 `todo/{ticket}/`에 남는다.
- **완료 티켓**: 모든 phase와 `overview.md`가 `done/{ticket}/`에 모인다.
- `overview.md`는 티켓의 활성 위치를 따라간다: 착수 시 `todo`→`doing`, 전체 완료 시 `doing`→`done`.
- 참조 규약: **다른 문서에서 티켓/phase를 참조할 때는 상태 폴더(`todo`/`doing`/`done`)를 경로에
  하드코딩하지 말고 이름으로 지칭한다** — 이동할 때마다 참조가 깨지는 것을 막기 위함이다.
  (예: "`ai-tickets/todo/foo`" ✗ → "`ai-tickets`의 `foo` 티켓" ✓)

---

## 티켓 폴더 네이밍

`todo/` 하위 폴더명은 작업 내용을 명확히 표현하는 **kebab-case** 로 짓는다.

```
좋은 예: samsung-health-step-integration
         r8-implementation
         pose-drawer-empty-progress-fix

나쁜 예: task1, fix, new-feature
```

---

## 티켓 파일 구조

각 티켓 폴더 안은 아래 구조를 따른다.

```
{ticket-name}/
├── overview.md              # 전체 개요 (목적, 배경, 하위 티켓 목록, 선행 관계)
├── phase1_{title}.md        # 세부 티켓 1
├── phase2_{title}.md        # 세부 티켓 2
└── phase{n}_{title}.md      # ...
```

### `overview.md`

- 작업의 **목적과 배경** — 왜 이 작업이 필요한지
- **하위 티켓 목록** — 파일명과 한 줄 요약
- **선행 관계** — 어떤 phase 가 먼저 완료되어야 하는지

### `phase{n}_{title}.md`

- `{n}`: 순서를 나타내는 숫자 (1부터 시작)
- `{title}`: 작업 내용을 나타내는 kebab-case
- 포함 내용:
  - 문제/목표
  - 수정 대상 파일과 위치
  - 구체적인 구현 방향
  - 검증 방법

예시:
```
phase1_error-handling.md
phase2_permission-callback.md
phase3_aggregate-optimization.md
```

#### 서브페이즈 표기 (선택)

phase 하나가 여러 하위 작업으로 나뉘는 큰 티켓은 `overview.md` 대신 `00_overview.md`를 쓰고
`p{n}-{m}_{title}.md`(예: `p1-1_error-handling-resolvable.md`, `p2-1_...`) 형식의 서브페이즈로 세분화해도 된다.
(예: `todo/samsung-health-step-integration/`)

---

## 수행 절차

1. `todo/` 에서 착수할 티켓을 고른다. `overview.md` 와 **첫 phase** 를 `doing/{ticket}/` 으로 옮긴다
   (나머지 phase 는 `todo/{ticket}/` 에 남긴다).
2. `overview.md` 를 먼저 읽어 전체 맥락과 선행 관계를 파악한다.
3. `doing/` 의 phase 를 수행한다.
4. 그 phase 가 완료되면: 파일 상단·`overview.md` 상태표를 갱신하고, phase 파일을 `done/{ticket}/` 으로 옮긴 뒤
   다음 phase 를 `todo/{ticket}/` 에서 `doing/{ticket}/` 으로 당겨온다. (`doing/` 엔 항상 phase 1개.)
5. 모든 phase 완료 → `overview.md` 도 `done/{ticket}/` 으로 옮긴다. 그러면 `doing/` 에서 해당 티켓이 사라진다.

```bash
# 착수: overview + 첫 phase를 doing으로
git mv ai-tickets/todo/{ticket}/overview.md   ai-tickets/doing/{ticket}/
git mv ai-tickets/todo/{ticket}/phase1_*.md   ai-tickets/doing/{ticket}/
# phase 완료: done으로 보내고 다음 phase를 doing으로
git mv ai-tickets/doing/{ticket}/phase1_*.md  ai-tickets/done/{ticket}/
git mv ai-tickets/todo/{ticket}/phase2_*.md   ai-tickets/doing/{ticket}/
# 티켓 전체 완료: overview도 done으로
git mv ai-tickets/doing/{ticket}/overview.md  ai-tickets/done/{ticket}/
```

---

## 기존 티켓 호환

`done/` 에는 완료된 phase(진행 중 티켓의 일부), 완료된 티켓 전체, 그리고 이전 방식
(`ticket.md`, `step-XX-...md` 등)으로 작성된 티켓이 섞여 있을 수 있다.
신규 티켓은 위 구조를 따르고, 기존 티켓은 수정하지 않는다.
