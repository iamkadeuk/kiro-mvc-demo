---
inclusion: always
---

# 프로젝트 개발 규칙

목적: Kiro가 이 간단한 MVC 예제의 구조와 규칙을 일관되게 유지하도록 한다.

- Controller → Service → Repository의 역할을 분리한다.
- 비즈니스 규칙은 Service에, 화면 상태는 Vue 컴포넌트에 작성한다.
- Task API의 기존 호환성을 유지하며, priority가 null이면 MEDIUM을 기본값으로 사용한다.
- dueDate는 선택 입력값으로 처리한다.
- 수정 후 백엔드 테스트와 프런트엔드 빌드를 실행한다.
