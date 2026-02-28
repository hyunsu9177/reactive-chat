# Reactive Chat

Spring Boot + Kafka 기반의 실시간 WebSocket 채팅 애플리케이션입니다.

## 주요 기능

- 실시간 그룹 채팅 (WebSocket/STOMP 기반)
- 닉네임 입력 및 사용자 식별
- 접속자 목록 실시간 표시
- 입장/퇴장 시스템 메시지
- 메시지 타임스탬프
- 메시지 입력 검증 (sender/content 필수, 최대 500자)
- 반응형 UI (모바일 지원)

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.4.3, Spring WebFlux, Spring WebSocket (STOMP) |
| Messaging | Apache Kafka |
| Frontend | Vanilla HTML/JS, SockJS, STOMP.js |
| Infra | Docker Compose (Kafka, Zookeeper, Redis, PostgreSQL) |
| Language | Java 21 |
| Build | Gradle |

## 시작하기

### 사전 준비

- Java 21 (Eclipse Adoptium)
- Docker & Docker Compose

### 1. 인프라 실행

```sh
docker-compose up -d
```

Kafka, Zookeeper, Redis, PostgreSQL이 실행됩니다.

### 2. 애플리케이션 실행

```sh
./gradlew bootRun
```

### 3. 접속

브라우저에서 [http://localhost:8080](http://localhost:8080)에 접속합니다.

## 아키텍처

```
브라우저 (SockJS/STOMP)
    ↓
WebSocket 엔드포인트 (/ws)
    ↓
ChatController
    ↓
ChatMessageProducer → Kafka 토픽 (chat-message)
    ↓
ChatMessageConsumer → SimpMessagingTemplate
    ↓
/topic/public → 전체 클라이언트에 브로드캐스트
```

- 클라이언트는 SockJS/STOMP로 WebSocket 서버에 연결
- 메시지는 Kafka를 거쳐 분산 처리 후 전체 클라이언트에 전달
- 접속자 관리는 인메모리(`OnlineUserService`)로 처리되며, 연결/해제 이벤트를 감지하여 `/topic/user-status`로 브로드캐스트
- Kafka 토픽명/컨슈머 그룹은 `application.yml`에서 설정 관리

## API 엔드포인트

| 엔드포인트 | 타입 | 설명 |
|------------|------|------|
| `/ws` | WebSocket | SockJS WebSocket 엔드포인트 |
| `/app/chat.sendMessage` | STOMP | 채팅 메시지 전송 |
| `/app/chat.addUser` | STOMP | 사용자 입장 |
| `/topic/public` | STOMP (구독) | 채팅 메시지 수신 |
| `/topic/user-status` | STOMP (구독) | 접속자 상태 변경 수신 |
| `/api/online-users` | REST GET | 접속자 목록 조회 |
| `/api/online-users/count` | REST GET | 접속자 수 조회 |

## 인프라 구성 (Docker Compose)

| 서비스 | 포트 | 비고 |
|--------|------|------|
| Kafka | 9092 | 메시지 브로커 |
| Zookeeper | 2181 | Kafka 코디네이션 |
| Redis | 6379 | 설정 완료, 추후 활용 예정 |
| PostgreSQL | 5432 | R2DBC 설정 완료 (`chatdb`), 추후 활용 예정 |
