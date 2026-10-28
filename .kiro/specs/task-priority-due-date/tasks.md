# Implementation Plan: 작업 우선순위 및 마감일 관리

## Overview

기존 Vue 3 + Spring Boot MVC 작업 보드에 우선순위(Priority)와 마감일(Due Date) 관리 기능을 추가한다.
백엔드는 `Priority` 열거형 신규 추가 → `TaskItem` / `TaskRequest` / `TaskResponse` 필드 확장 → `TaskService` 비즈니스 로직 변경 → `ApiExceptionHandler` 예외 핸들러 추가 순으로 진행한다.
프런트엔드는 `TaskCard.vue` → `TaskForm.vue` → `App.vue` 순으로 확장하고, 마지막에 빌드를 검증한다.

## Tasks

- [x] 1. 백엔드 모델 계층 구축
  - [x] 1.1 `Priority` 열거형 신규 생성
    - `backend/src/main/java/com/example/kiromvc/task/Priority.java` 파일을 신규 생성한다.
    - `LOW`, `MEDIUM`, `HIGH` 세 값만 정의한다.
    - _요구사항: 1.1, 1.3_

  - [x] 1.2 `TaskItem` 엔티티에 `priority`, `dueDate` 필드 추가
    - `@Enumerated(EnumType.STRING)` / `@Column(nullable = false, length = 10)`으로 `priority` 필드를 추가한다.
    - `@Column` nullable로 `java.time.LocalDate dueDate` 필드를 추가한다.
    - 생성자를 `(String title, String description, boolean completed, Priority priority, LocalDate dueDate)` 시그니처로 확장한다.
    - `update()` 메서드에 `Priority priority`, `LocalDate dueDate` 파라미터를 추가한다.
    - `getPriority()`, `getDueDate()` getter를 추가한다.
    - _요구사항: 1.1, 2.1_

  - [x] 1.3 `TaskRequest` 레코드에 `priority`, `dueDate` 필드 추가
    - `Priority priority` 필드를 nullable 선택 입력으로 추가한다 (`null`이면 서비스 레이어에서 기본값 처리).
    - `@JsonFormat(pattern = "yyyy-MM-dd")`을 적용한 `java.time.LocalDate dueDate` 필드를 nullable 선택 입력으로 추가한다.
    - _요구사항: 1.3, 2.2, 2.6_

  - [x] 1.4 `TaskResponse` 레코드에 `priority`, `dueDate`, `overdue` 필드 추가 및 `from()` 팩터리 수정
    - `Priority priority`, `java.time.LocalDate dueDate`, `boolean overdue` 필드를 추가한다 (기존 필드 순서·타입 유지).
    - `from(TaskItem task)` 팩터리 내에서 `overdue` 계산 로직을 구현한다:
      - `completed=true` → `false`
      - `dueDate=null` → `false`
      - `dueDate`가 `LocalDate.now()` 이전 && `completed=false` → `true`
      - 그 외 → `false`
    - _요구사항: 1.5, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 8.3_

- [x] 2. 백엔드 서비스 및 예외 처리 변경
  - [x] 2.1 `TaskService.create()` 메서드에 `priority` 기본값 처리 및 `dueDate` 전달 추가
    - `request.priority()`가 `null`이면 `Priority.MEDIUM`으로 대체한다.
    - `TaskItem` 생성자 호출 시 `priority`와 `dueDate`를 전달한다.
    - _요구사항: 1.2, 2.3, 2.4, 8.1_

  - [x] 2.2 `TaskService.update()` 메서드에 `priority` 기존 값 유지 및 `dueDate` 전달 추가
    - `request.priority()`가 `null`이면 `task.getPriority()` (기존 저장 값)를 사용한다.
    - `task.update()` 호출 시 `priority`와 `dueDate`를 전달한다.
    - _요구사항: 1.6, 2.4, 8.2_

  - [x] 2.3 `ApiExceptionHandler`에 `HttpMessageNotReadableException` 핸들러 추가
    - `@ExceptionHandler(HttpMessageNotReadableException.class)` 핸들러 메서드를 추가한다.
    - HTTP 400 + RFC 9457 `ProblemDetail`을 반환하며, `title`은 "잘못된 요청 형식", `detail`에는 날짜 형식과 우선순위 허용 값을 안내하는 메시지를 포함한다.
    - _요구사항: 1.4, 2.6_

- [x] 3. 체크포인트 — 백엔드 기본 빌드 확인
  - 모든 테스트가 통과하는지 확인한다. 문제가 생기면 사용자에게 보고한다.

- [x] 4. 백엔드 테스트 작성

  - [x] 4.1 `TaskServiceTest`에 기존 동작 유지 확인 및 `priority` / `dueDate` / `overdue` 단위 테스트 추가
    - `priority=null` 생성 시 `MEDIUM` 기본값 설정을 확인한다.
    - `priority=LOW` 생성 후 `priority=null`로 업데이트 시 `LOW` 유지를 확인한다.
    - `overdue` 계산 5가지 케이스를 검증한다: 과거+미완료→`true`, 완료+과거→`false`, `dueDate=null`→`false`, 오늘→`false`, 미래→`false`.
    - 기존 두 테스트(`createTrimsInputAndSavesTask`, `findByIdThrowsWhenTaskDoesNotExist`)가 컴파일·통과되도록 `TaskRequest` 생성자 호출을 신규 시그니처에 맞게 수정한다.
    - _요구사항: 1.2, 1.6, 3.2, 3.3, 3.4, 3.5_

  - [ ]* 4.2 jqwik 의존성 추가 및 Property 1 테스트 작성 — `priority null → MEDIUM 기본값`
    - `pom.xml`에 jqwik 의존성을 추가한다 (`net.jqwik:jqwik`).
    - `TaskServicePropertyTest.java`를 신규 생성하고 `@Property(tries = 100)` 테스트를 작성한다.
    - `@ForAll` 어노테이션으로 임의의 유효한 `title`과 `description`을 생성하고, `priority=null`인 `TaskRequest`로 `create()`를 호출한다.
    - 반환된 `TaskResponse.priority()`가 `MEDIUM`임을 단언한다.
    - `// Feature: task-priority-due-date, Property 1: priority null → MEDIUM 기본값` 주석을 포함한다.
    - _요구사항: 1.2, 8.1_

  - [ ]* 4.3 Property 2 테스트 작성 — `priority 라운드트립 보존`
    - `@ForAll Priority priority`와 임의의 유효한 `title`로 `create()`를 호출한다.
    - 반환된 `TaskResponse.priority()`가 입력 `priority`와 동일함을 단언한다.
    - `// Feature: task-priority-due-date, Property 2: priority 라운드트립 보존` 주석을 포함한다.
    - _요구사항: 1.5_

  - [ ]* 4.4 Property 3 테스트 작성 — `update 시 priority null → 기존 값 유지`
    - 임의의 `Priority`로 작업을 생성한 후 `priority=null`인 `TaskRequest`로 `update()`를 호출한다.
    - 반환된 `TaskResponse.priority()`가 생성 시 사용한 값과 동일함을 단언한다.
    - `// Feature: task-priority-due-date, Property 3: update 시 priority null → 기존 값 유지` 주석을 포함한다.
    - _요구사항: 1.6, 8.2_

  - [ ]* 4.5 Property 4 테스트 작성 — `overdue — 과거 날짜 + 미완료`
    - `TaskResponse.from()`을 직접 호출하며 repository mock이 불필요하다.
    - `@ForAll`로 오늘(`LocalDate.now()`)보다 이전인 임의의 `dueDate`를 생성하고 `completed=false`인 `TaskItem`을 구성한다.
    - `overdue`가 `true`임을 단언한다.
    - `// Feature: task-priority-due-date, Property 4: overdue — 과거 날짜 + 미완료` 주석을 포함한다.
    - _요구사항: 3.2_

  - [ ]* 4.6 Property 5 테스트 작성 — `overdue — completed=true이면 항상 false`
    - 임의의 `dueDate`(과거·오늘·미래·null 포함)와 `completed=true`인 `TaskItem`에 대해 `overdue`가 `false`임을 단언한다.
    - `// Feature: task-priority-due-date, Property 5: overdue — completed=true이면 항상 false` 주석을 포함한다.
    - _요구사항: 3.3_

  - [ ]* 4.7 Property 6 테스트 작성 — `overdue — 오늘 날짜는 false`
    - `dueDate = LocalDate.now()`, `completed=false`인 `TaskItem`에 대해 `overdue`가 `false`임을 단언한다.
    - `// Feature: task-priority-due-date, Property 6: overdue — 오늘 날짜는 false` 주석을 포함한다.
    - _요구사항: 3.5_

- [x] 5. 체크포인트 — 백엔드 전체 테스트 통과 확인
  - 모든 단위 테스트와 속성 기반 테스트가 통과하는지 확인한다. 문제가 생기면 사용자에게 보고한다.

- [x] 6. 프런트엔드 컴포넌트 변경

  - [x] 6.1 `TaskCard.vue` — `priority`, `dueDate`, `overdue` 표시 및 편집 모드 확장
    - `draft` reactive 객체에 `priority: 'MEDIUM'`, `dueDate: ''` 필드를 추가한다.
    - `watch(props.task, ...)` 내에서 `draft.priority`와 `draft.dueDate`를 동기화한다.
    - `dueDateFormatted` computed를 추가한다: `dueDate`가 있으면 `YYYY년 MM월 DD일` 형식으로 변환, 없으면 `null`.
    - `toggleCompleted()`에 `priority: props.task.priority`, `dueDate: props.task.dueDate || null`을 포함한다.
    - `save()`에 `priority: draft.priority`, `dueDate: draft.dueDate || null`을 포함한다.
    - 보기 모드 템플릿에 우선순위 레이블(`<span class="priority-label">`), 마감일(`<span v-if="dueDateFormatted" class="due-date">`), 기한 초과 레이블(`<span v-if="task.overdue" class="overdue-label">기한 초과</span>`)을 추가한다.
    - `<article>` 클래스에 `:class="{ completed: task.completed, overdue: task.overdue }"`를 적용한다.
    - 편집 모드 템플릿에 우선순위 `<select>` 입력과 마감일 `<input type="date">` 입력을 추가한다.
    - _요구사항: 6.4, 6.5, 7.1, 7.2, 7.3, 7.4_

  - [x] 6.2 `TaskForm.vue` — `priority`, `dueDate` 입력 필드 추가
    - `form` reactive 객체에 `priority: 'MEDIUM'`, `dueDate: ''` 필드를 추가한다.
    - `submit()` emit 객체에 `priority: form.priority`, `dueDate: form.dueDate || null`을 포함하고, 제출 후 `form.priority = 'MEDIUM'`, `form.dueDate = ''`로 초기화한다.
    - `initialTask` prop(선택 입력)을 추가하고, 전달된 경우 `onMounted`에서 `form.priority`와 `form.dueDate`를 채운다.
    - 템플릿에 우선순위 `<select>` 입력(옵션: `LOW`, `MEDIUM`, `HIGH`, 기본값 `MEDIUM`)과 마감일 `<input type="date">` 입력을 추가한다.
    - _요구사항: 6.1, 6.2, 6.3, 6.6_

  - [x] 6.3 `App.vue` — 우선순위 필터 및 마감 임박 순 정렬 추가
    - `priorityFilter` ref(`'all'`)와 `sortBy` ref(`'createdAt'`)를 추가한다.
    - 기존 `filteredTasks` computed를 `sortedAndFilteredTasks`로 교체하며 세 단계 파이프라인을 구현한다:
      1. 완료 상태 필터(`all` / `active` / `done`)
      2. 우선순위 필터(`all` / `LOW` / `MEDIUM` / `HIGH`)
      3. 정렬: `sortBy === 'dueDate'`이면 마감 임박 순(`dueDate` 오름차순, `null`은 뒤, 동일 `dueDate`는 `createdAt` 내림차순), 기본은 `createdAt` 내림차순.
    - 템플릿의 `filteredTasks` 참조를 `sortedAndFilteredTasks`로 교체한다.
    - 기존 완료 상태 필터 버튼 그룹 옆에 우선순위 필터 버튼 그룹(`전체 / LOW / MEDIUM / HIGH`)을 추가한다.
    - 정렬 선택 `<select>`(`기본(등록 순) / 마감 임박 순`)를 추가한다.
    - _요구사항: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [x] 7. 프런트엔드 테스트 환경 구성 및 속성 기반 테스트 작성

  - [x] 7.1 Vitest 및 fast-check 의존성 추가
    - `frontend` 디렉터리에서 `vitest`, `@vitest/coverage-v8`, `fast-check`, `@vue/test-utils`, `jsdom`을 개발 의존성으로 추가한다.
    - `vite.config.js`(또는 `vitest.config.js`)에 Vitest 설정(`environment: 'jsdom'`)을 추가한다.
    - `package.json`에 `"test": "vitest --run"` 스크립트를 추가한다.
    - _요구사항: 4.1, 4.2, 4.4, 5.2, 5.3, 5.4_

  - [ ]* 7.2 `sortedAndFilteredTasks` 필터·정렬 로직을 순수 함수로 추출하고 Property 7 테스트 작성 — `우선순위 필터 — 렌더링된 모든 항목이 조건 충족`
    - `frontend/src/utils/taskFilters.js`를 신규 생성하고, `App.vue`의 `sortedAndFilteredTasks` 계산 로직을 `computeSortedAndFiltered(tasks, filter, priorityFilter, sortBy)` 순수 함수로 추출한다. `App.vue`는 이 함수를 호출하도록 변경한다.
    - `frontend/src/utils/__tests__/taskFilters.test.js`를 신규 생성한다.
    - `fc.assert(fc.property(...))` 형태로, 임의의 작업 배열과 `LOW | MEDIUM | HIGH` 중 하나의 `priorityFilter`에 대해 반환된 모든 작업의 `priority`가 해당 값과 동일함을 단언한다.
    - `// Feature: task-priority-due-date, Property 7: 우선순위 필터 — 렌더링된 모든 항목이 조건 충족` 주석을 포함한다.
    - _요구사항: 4.2_

  - [ ]* 7.3 Property 8 테스트 작성 — `완료 상태 + 우선순위 AND 필터`
    - 임의의 작업 배열, 완료 상태 필터(`active` | `done`), 우선순위 필터(`LOW` | `MEDIUM` | `HIGH`) 조합에 대해 반환된 모든 작업이 두 조건을 동시에 만족함을 단언한다.
    - `// Feature: task-priority-due-date, Property 8: 완료 상태 + 우선순위 AND 필터` 주석을 포함한다.
    - _요구사항: 4.4_

  - [ ]* 7.4 Property 9 테스트 작성 — `마감 임박 순 정렬 — 인접 쌍 순서 불변`
    - 임의의 작업 배열에 `sortBy='dueDate'`를 적용했을 때 결과의 인접한 두 작업 `a`, `b`에 대해 다음을 단언한다:
      - `a.dueDate !== null && b.dueDate !== null` → `a.dueDate ≤ b.dueDate`
      - `a.dueDate !== null && b.dueDate === null` → 항상 성립
      - `a.dueDate === null && b.dueDate !== null` → 불가능(null은 항상 뒤)
      - `a.dueDate === b.dueDate` → `a.createdAt ≥ b.createdAt`
    - `// Feature: task-priority-due-date, Property 9: 마감 임박 순 정렬 — 인접 쌍 순서 불변` 주석을 포함한다.
    - _요구사항: 5.2, 5.3, 5.4_

- [x] 8. 프런트엔드 빌드 검증
  - [x] 8.1 `pnpm run build`로 프런트엔드 빌드가 오류 없이 완료되는지 확인한다.
    - `frontend` 디렉터리에서 `pnpm run build`를 실행한다.
    - 빌드 출력에 오류가 없음을 확인한다.
    - _요구사항: 4.1, 4.2, 4.3, 4.4, 5.1_

- [x] 9. 최종 체크포인트 — 전체 테스트 통과 확인
  - 모든 백엔드 테스트(`./mvnw test`)와 프런트엔드 테스트(`pnpm --filter frontend run test`)가 통과하는지 확인한다. 문제가 생기면 사용자에게 보고한다.

## Notes

- `*` 표시가 있는 서브태스크는 선택 사항으로, MVP를 빠르게 구성할 경우 건너뛸 수 있다.
- 각 태스크는 특정 요구사항을 참조하므로 추적성을 보장한다.
- 체크포인트는 점진적 검증을 위한 중간 확인 지점이다.
- 속성 기반 테스트(PBT)는 설계 문서의 Correctness Properties 섹션에 정의된 9개 속성을 검증한다.
- `taskApi.js`는 task 객체를 JSON 직렬화하여 전송하므로 별도 변경이 불필요하다.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["1.3", "1.4"] },
    { "id": 3, "tasks": ["2.1", "2.2", "2.3"] },
    { "id": 4, "tasks": ["4.1", "6.1", "6.2"] },
    { "id": 5, "tasks": ["4.2", "4.3", "4.4", "4.5", "4.6", "4.7", "6.3", "7.1"] },
    { "id": 6, "tasks": ["7.2", "7.3", "7.4", "8.1"] }
  ]
}
```
