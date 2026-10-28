# Requirements Document

## Introduction

기존 Vue 3 + Spring Boot MVC 작업 보드에 **우선순위(Priority)** 와 **마감일(Due Date)** 관리 기능을 추가한다.
사용자는 작업을 생성하거나 수정할 때 우선순위(LOW / MEDIUM / HIGH)와 선택적 마감일을 지정할 수 있으며, 마감일이 지난 미완료 작업은 기한 초과(Overdue) 상태로 표시된다. 프런트엔드는 우선순위 필터와 마감 임박 순 정렬을 제공한다. 기존 `GET /api/tasks`, `POST /api/tasks`, `PUT /api/tasks/{id}`, `DELETE /api/tasks/{id}` API의 하위 호환성을 유지한다.

## Glossary

- **Task**: 작업 보드의 단일 할 일 항목. `TaskItem` 엔티티로 영속화된다.
- **Priority**: 작업의 중요도를 나타내는 열거형. `LOW`, `MEDIUM`, `HIGH` 세 값을 가진다.
- **Due Date**: 작업 완료 목표일. `LocalDate` 타입으로 저장되며 선택 입력이다.
- **Overdue**: 미완료(`completed = false`) 상태이며 Due Date가 오늘 날짜보다 이전인 작업의 상태.
- **TaskController**: HTTP 요청을 수신하고 응답을 반환하는 Spring MVC 컨트롤러.
- **TaskService**: 비즈니스 규칙과 트랜잭션을 담당하는 서비스 레이어.
- **TaskRepository**: 데이터베이스 접근을 담당하는 JPA 리포지터리.
- **TaskRequest**: 생성·수정 요청의 입력 DTO (Java record).
- **TaskResponse**: API 응답의 출력 DTO (Java record).
- **TaskForm**: 작업 생성·수정 입력 UI를 제공하는 Vue 컴포넌트.
- **TaskCard**: 개별 작업 정보를 표시하고 인라인 수정을 지원하는 Vue 컴포넌트.
- **App**: 필터링·정렬 상태를 관리하고 전체 작업 목록을 렌더링하는 최상위 Vue 컴포넌트.

## Requirements

### Requirement 1: 우선순위 필드 추가

**User Story:** 개발자로서, 작업에 우선순위를 지정하고 싶다. 그래야 어떤 작업을 먼저 처리해야 하는지 한눈에 알 수 있다.

#### Acceptance Criteria

1. THE TaskItem SHALL `priority` 필드를 가지며, 허용 값은 `LOW`, `MEDIUM`, `HIGH`이다.
2. IF `TaskRequest`의 `priority` 필드가 없거나 `null`인 경우, THEN THE TaskService SHALL `priority`를 `MEDIUM`으로 설정하고 HTTP 201 응답을 반환한다.
3. THE TaskRequest SHALL `priority` 필드를 선택 입력으로 허용하며, 허용 값은 `LOW`, `MEDIUM`, `HIGH`이다.
4. IF `TaskRequest`에 `LOW`, `MEDIUM`, `HIGH` 이외의 `priority` 값이 전달된 경우, THEN THE TaskController SHALL HTTP 400과 RFC 9457 Problem Details 형식의 오류 응답을 반환한다.
5. THE TaskResponse SHALL `priority` 필드를 포함하여 해당 TaskItem에 저장된 우선순위 값을 반환한다.
6. IF 기존 작업 수정 요청에 `priority` 필드가 없거나 `null`인 경우, THEN THE TaskService SHALL 해당 TaskItem의 기존 `priority` 값을 유지한다.

### Requirement 2: 마감일 필드 추가

**User Story:** 개발자로서, 작업에 마감일을 설정하고 싶다. 그래야 일정 관리를 체계적으로 할 수 있다.

#### Acceptance Criteria

1. THE TaskItem SHALL `dueDate` 필드를 가지며, 타입은 `LocalDate`이고 null 허용이다.
2. THE TaskRequest SHALL `dueDate` 필드를 선택 입력으로 허용하며, 형식은 `YYYY-MM-DD`이다.
3. IF `TaskRequest`의 `dueDate`가 오늘 날짜보다 이전인 경우, THEN THE TaskService SHALL 해당 값을 그대로 저장한다.
4. IF `TaskRequest`에 `dueDate`가 없거나 `null`인 경우, THEN THE TaskService SHALL `dueDate`를 `null`로 저장한다.
5. THE TaskResponse SHALL `dueDate` 필드를 포함하며, 값이 있는 경우 `YYYY-MM-DD` 형식의 문자열로 반환하고, 값이 없는 경우 `null`을 반환한다.
6. IF `TaskRequest`의 `dueDate` 값이 `YYYY-MM-DD` 형식이 아닌 경우, THEN THE TaskController SHALL 요청을 거부하고 유효하지 않은 날짜 형식임을 나타내는 오류 응답을 반환한다.

### Requirement 3: 기한 초과(Overdue) 상태 계산

**User Story:** 개발자로서, 마감일이 지난 미완료 작업을 즉시 식별하고 싶다. 그래야 지연된 작업에 빠르게 대응할 수 있다.

#### Acceptance Criteria

1. THE TaskResponse SHALL `overdue` 필드를 포함한다.
2. IF `completed`가 `false`이고 `dueDate`가 서버의 시스템 기본 시간대 기준 오늘 날짜(`LocalDate.now()`)보다 이전인 경우, THEN THE TaskService SHALL `overdue`를 `true`로 계산한다.
3. IF `completed`가 `true`인 경우, THEN THE TaskService SHALL `overdue`를 `false`로 계산한다.
4. IF `dueDate`가 `null`인 경우, THEN THE TaskService SHALL `overdue`를 `false`로 계산한다.
5. IF `dueDate`가 서버의 시스템 기본 시간대 기준 오늘 날짜와 동일한 경우, THEN THE TaskService SHALL `overdue`를 `false`로 계산한다.
6. THE TaskService SHALL `overdue` 값을 TaskResponse를 생성할 때 `completed` 및 `dueDate` 필드를 기반으로 계산하며, 데이터베이스에 저장하지 않는다.

### Requirement 4: 우선순위 필터

**User Story:** 개발자로서, 특정 우선순위의 작업만 모아서 보고 싶다. 그래야 집중해야 할 작업에 빠르게 접근할 수 있다.

#### Acceptance Criteria

1. THE App SHALL 기존 전체/진행 중/완료 필터 외에 우선순위 필터 옵션을 제공하며, 기본값은 `all`이다. 우선순위 필터 값은 `all`, `LOW`, `MEDIUM`, `HIGH`이다.
2. WHEN 사용자가 우선순위 필터를 `LOW`, `MEDIUM`, `HIGH` 중 하나로 선택하는 경우, THE App SHALL 해당 우선순위 값과 일치하는 작업만 렌더링한다.
3. IF 우선순위 필터가 `all`인 경우, THEN THE App SHALL 우선순위와 무관하게 모든 작업을 렌더링한다.
4. WHILE 완료 상태 필터와 우선순위 필터가 동시에 활성화된 경우, THE App SHALL 두 조건을 모두 충족하는 작업만 렌더링한다.
5. IF 완료 상태 필터와 우선순위 필터의 조합에 일치하는 작업이 없는 경우, THEN THE App SHALL 작업이 없음을 안내하는 메시지를 렌더링한다.

### Requirement 5: 마감 임박 순 정렬

**User Story:** 개발자로서, 마감일이 가까운 작업을 목록 위에 표시하고 싶다. 그래야 기한을 놓치는 일을 줄일 수 있다.

#### Acceptance Criteria

1. THE App SHALL 마감 임박 순 정렬 옵션을 제공한다.
2. WHEN 마감 임박 순 정렬이 선택된 경우, THE App SHALL `dueDate`가 가장 이른 작업을 목록 앞에 배치한다.
3. WHEN 마감 임박 순 정렬이 선택되고 두 작업의 `dueDate`가 동일한 경우, THE App SHALL 두 작업의 순서를 `createdAt` 기준 내림차순(최신 등록 우선)으로 정한다.
4. WHEN 마감 임박 순 정렬이 선택되고 `dueDate`가 `null`인 작업이 있는 경우, THE App SHALL `dueDate`가 `null`인 작업을 `dueDate`가 있는 작업보다 목록 뒤에 배치하며, `dueDate`가 `null`인 작업끼리는 `createdAt` 기준 내림차순으로 정렬한다.
5. WHEN 마감 임박 순 정렬이 선택되지 않은 경우, THE App SHALL 기존 `createdAt` 기준 내림차순(최신 등록 우선)으로 작업을 표시한다.
6. THE App SHALL 초기 진입 시 기본 정렬 기준을 `createdAt` 내림차순으로 적용한다.

### Requirement 6: 프런트엔드 입력 UI 확장

**User Story:** 개발자로서, 작업을 생성하거나 수정할 때 우선순위와 마감일을 UI에서 직접 입력하고 싶다. 그래야 API를 직접 호출하지 않고도 두 필드를 관리할 수 있다.

#### Acceptance Criteria

1. THE TaskForm SHALL 우선순위 선택 입력(`<select>`)을 제공하며, 옵션은 `LOW`, `MEDIUM`, `HIGH`이고 기본값은 `MEDIUM`이다.
2. THE TaskForm SHALL 마감일 날짜 입력(`<input type="date">`)을 제공하며, 선택 입력으로 빈 값을 허용한다.
3. WHEN TaskForm을 제출하는 경우, THE TaskForm SHALL `priority`와 `dueDate`를 포함한 작업 객체를 `submit` 이벤트로 부모 컴포넌트에 전달하며, `dueDate`가 입력되지 않은 경우 해당 필드 값은 `null`로 전달한다.
4. WHILE TaskCard가 편집 모드인 경우, THE TaskCard SHALL 현재 작업의 `priority` 값이 선택된 상태로 표시되는 `<select>` 입력과 현재 작업의 `dueDate` 값이 채워진 상태로 표시되는 날짜 입력을 카드 내부에서 페이지 이동 없이 제공한다.
5. WHEN TaskCard가 편집 모드에서 저장되는 경우, THE TaskCard SHALL 변경된 `priority`와 `dueDate`를 포함한 작업 객체를 `update` 이벤트로 부모 컴포넌트에 전달하며, `dueDate`가 비어 있는 경우 해당 필드 값은 `null`로 전달한다.
6. WHEN TaskForm에 기존 작업 데이터가 전달되는 경우, THE TaskForm SHALL 해당 작업의 `priority` 값을 선택 입력에 선택된 상태로 표시하고 `dueDate` 값을 날짜 입력에 채워진 상태로 표시한다.

### Requirement 7: 프런트엔드 작업 카드 표시

**User Story:** 개발자로서, 작업 카드에서 우선순위·마감일·기한 초과 여부를 한눈에 확인하고 싶다. 그래야 각 작업의 상태를 빠르게 파악할 수 있다.

#### Acceptance Criteria

1. THE TaskCard SHALL 현재 작업의 `priority` 값을 `LOW`, `MEDIUM`, `HIGH` 중 하나의 텍스트 레이블로 표시한다.
2. IF `dueDate`가 `null`이 아닌 경우, THEN THE TaskCard SHALL 해당 날짜를 `YYYY년 MM월 DD일` 형식으로 표시한다.
3. IF `overdue`가 `true`인 경우, THEN THE TaskCard SHALL 기한 초과 상태를 "기한 초과" 텍스트 레이블 또는 강조 배경으로 나타내어 `overdue`가 `false`인 카드와 시각적으로 구분되도록 한다.
4. IF `dueDate`가 `null`인 경우, THEN THE TaskCard SHALL 마감일 영역을 렌더링하지 않는다.

### Requirement 8: API 하위 호환성 유지

**User Story:** 개발자로서, 새 필드를 추가한 이후에도 기존 API 클라이언트가 정상 동작하기를 원한다. 그래야 클라이언트를 동시에 수정하지 않아도 되는 점진적 배포가 가능하다.

#### Acceptance Criteria

1. WHEN `priority`와 `dueDate` 필드 없이 기존 클라이언트가 `POST /api/tasks`를 호출하는 경우, THE TaskController SHALL 요청을 정상 처리하고 `priority`를 `MEDIUM`, `dueDate`를 `null`로 저장하여 HTTP 201을 반환한다.
2. WHEN `priority`와 `dueDate` 필드 없이 기존 클라이언트가 `PUT /api/tasks/{id}`를 호출하는 경우, THE TaskController SHALL 요청을 정상 처리하고 `priority`는 기존 저장 값을 유지하며 `dueDate`를 `null`로 저장하여 HTTP 200을 반환한다.
3. THE TaskResponse SHALL 기존 응답 필드(`id`, `title`, `description`, `completed`, `createdAt`, `updatedAt`)를 변경 없이 유지하며, 신규 필드(`priority`, `dueDate`, `overdue`)는 추가(additive) 방식으로 응답에 포함된다.
4. THE TaskController SHALL 기존 엔드포인트 URL(`/api/tasks`, `/api/tasks/{id}`)과 HTTP 메서드(`GET`, `POST`, `PUT`, `DELETE`)를 변경하지 않는다.
