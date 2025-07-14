# reactive-chat

**Reactive WebSocket Chat App**  
React와 Spring Boot 기반의 실시간 웹소켓 채팅 애플리케이션입니다.

---

## ✨ 주요 기능

- 실시간 1:1 / 그룹 채팅 (WebSocket 기반)
- 닉네임/식별 기능
- 접속자 실시간 표시
- 메시지 타임스탬프
- 반응형 UI  
- (예정) 사용자 인증, 채팅방 생성, 파일 전송 등

---

## 🛠️ 기술 스택

- **Frontend**: React, SockJS, Stomp.js, Axios, CSS
- **Backend**: Spring Boot, Spring WebSocket(STOMP), Java
- **Build**: Gradle, npm/yarn
- **DB**: (옵션) H2, MySQL, PostgreSQL 등

---

## 🚀 시작하기

### 1. 백엔드(Spring Boot) 실행

```sh
cd backend
./gradlew bootRun
# 또는
./gradlew build
java -jar build/libs/reactive-chat-0.0.1-SNAPSHOT.jar
