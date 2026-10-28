# Kiro MVC Task Board

Kiro의 코드 탐색·생성·리팩터링 기능을 시험하기 위한 작은 풀스택 CRUD 예제입니다. Vue 3가 화면(View)을 담당하고, Spring Boot가 `Controller → Service → Repository` 구조로 요청을 처리합니다.

## 기술 스택

- Frontend: Vue 3, Vite
- Backend: Java 21, Spring Boot 4, Spring MVC, Spring Data JPA
- Database: H2 인메모리 DB
- 실행 옵션: 로컬 개발 서버 또는 Docker Compose

## MVC 구조

```text
Browser
  └─ Vue View (frontend/src)
       └─ REST API (/api/tasks)
            └─ TaskController       요청/응답, 유효성 검사
                 └─ TaskService     비즈니스 규칙, 트랜잭션
                      └─ TaskRepository  데이터 접근
                           └─ H2 Database
```

프런트엔드는 백엔드의 MVC 구조와 별도의 SPA입니다. Vue 컴포넌트가 View 역할을 하고 `taskApi.js`가 HTTP 요청을 캡슐화합니다.

## 기능

- 할 일 목록 조회
- 할 일 생성·수정·삭제
- 완료 상태 토글
- 전체/진행 중/완료 필터
- 입력값 검증과 RFC 9457 Problem Details 형식 오류 응답
- 예제 데이터 자동 등록

## 실행 방법

### 1. Docker Compose (가장 간단)

```bash
docker compose up --build
```

- 화면: http://localhost:5173
- API: http://localhost:8080/api/tasks
- H2 콘솔: http://localhost:8080/h2-console

### 2. 로컬 개발 서버

Java 21과 Node.js 22.12 이상이 필요합니다.

```bash
# 터미널 1
cd backend
mvn spring-boot:run

# 터미널 2
cd frontend
pnpm install
pnpm run dev
```

Vite 개발 서버가 `/api` 요청을 `localhost:8080`으로 프록시합니다.

## API

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/tasks` | 전체 조회 |
| `GET` | `/api/tasks/{id}` | 단건 조회 |
| `POST` | `/api/tasks` | 생성 |
| `PUT` | `/api/tasks/{id}` | 전체 수정 |
| `DELETE` | `/api/tasks/{id}` | 삭제 |

요청 예시:

```json
{
  "title": "Kiro로 서비스 계층 리팩터링",
  "description": "중복 검증 로직을 분리한다.",
  "completed": false
}
```

## Kiro에서 시험해 볼 프롬프트

1. `TaskController부터 데이터베이스까지 요청 흐름을 설명해 줘.`
2. `할 일에 dueDate 필드를 추가하고 프런트엔드 입력 UI도 수정해 줘.`
3. `제목으로 검색하는 API와 검색창을 추가해 줘.`
4. `TaskService의 테스트 케이스를 더 만들어 줘.`
5. `H2 대신 PostgreSQL을 사용하도록 Docker Compose를 바꿔 줘.`

## 테스트

```bash
cd backend
mvn test

cd ../frontend
pnpm install --frozen-lockfile
pnpm run build
```

