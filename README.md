reactive-chat
Reactive WebSocket Chat App
실시간 채팅 기능을 제공하는 React + Spring Boot 기반의 WebSocket 채팅 애플리케이션입니다.

Features
실시간 1:1/그룹 채팅 (WebSocket 기반)

사용자 닉네임/식별 기능

접속자 실시간 표시

메시지 타임스탬프 표시

반응형 UI

(추가 가능) 사용자 인증, 파일 전송, 채팅방 생성 등

Tech Stack
Frontend: React, SockJS, Stomp.js, Axios, CSS

Backend: Spring Boot, Spring WebSocket (STOMP), Java

Build: Gradle, npm/yarn

DB: (선택적) H2, MySQL, PostgreSQL 등

Getting Started
1. 백엔드(Spring Boot) 실행
sh
복사
편집
cd backend
./gradlew bootRun
# 또는
./gradlew build
java -jar build/libs/reactive-chat-0.0.1-SNAPSHOT.jar
기본 포트: 8080

2. 프론트엔드(React) 실행
sh
복사
편집
cd frontend
npm install
npm start
# 또는
yarn install
yarn start
기본 포트: 3000

3. 브라우저에서 실행
http://localhost:3000 접속
채팅 닉네임 입력 후 채팅 참여

Directory Structure
bash
복사
편집
reactive-chat/
 ├── backend/        # Spring Boot 서버
 │   └── src/
 │   └── build.gradle
 │   └── ...
 ├── frontend/       # React 프론트엔드
 │   └── src/
 │   └── package.json
 │   └── ...
 └── README.md
주요 사용법
서버와 프론트엔드를 모두 실행 후, 브라우저에서 채팅 UI에 접속합니다.

닉네임(또는 ID) 입력 후 채팅방에 입장합니다.

메시지 입력 후 Enter를 누르면 실시간으로 메시지가 전달됩니다.

환경 설정
프론트엔드에서 백엔드 서버 주소(WS, HTTP) 수정 필요 시:
/frontend/src/config.js 등에서 변경

WebSocket Endpoint:

/ws-chat (예: ws://localhost:8080/ws-chat)

To-do / 개선 아이디어
JWT 기반 사용자 인증

채팅 메시지 DB 저장/조회

파일/이미지 전송 기능

알림, 메시지 읽음/안읽음 표시

채팅방 목록/참여/퇴장 등 그룹채팅 기능

관리자(강퇴, 메시지 삭제 등)

테스트 코드 추가

참고
Spring WebSocket Docs

React

SockJS

Stomp.js

License
MIT License

