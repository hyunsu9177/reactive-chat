# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 및 실행 명령어

```bash
# 인프라 실행 (Kafka, Zookeeper, Redis, PostgreSQL)
docker-compose up -d

# 빌드
./gradlew build

# 애플리케이션 실행 (인프라가 먼저 실행되어야 함)
./gradlew bootRun

# 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build
```

앱은 `http://localhost:8080`에서 실행된다. Java 21 (Eclipse Adoptium) 필요.

## 아키텍처

Spring Boot 3.4.3 (WebFlux + WebSocket) 기반의 **실시간 WebSocket 채팅 애플리케이션**으로, Kafka를 통해 분산 메시징을 처리한다.

### 메시지 흐름

```
브라우저 (SockJS/STOMP) → WebSocket /ws → ChatController
  → ChatMessageProducer → Kafka 토픽 "chat-message"
  → ChatMessageConsumer → SimpMessagingTemplate → /topic/public → 전체 클라이언트
```

- **ChatController**: STOMP 목적지 `/app/chat.sendMessage`, `/app/chat.addUser` 처리
- **ChatController**: REST 엔드포인트 `/api/online-users`, `/api/online-users/count` 제공
- **OnlineUserService**: 접속 유저를 인메모리(`CopyOnWriteArraySet`)로 관리
- **WebSocketEventListener**: 연결/해제 이벤트를 감지하여 `/topic/user-status`로 유저 상태 브로드캐스트

### 주요 컨벤션

- **JSON**: Jackson에서 `snake_case` 프로퍼티 네이밍 사용 (`JacksonConfig`)
- **Lombok**: 모델 클래스에 사용
- **프론트엔드**: `src/main/resources/static/`에 바닐라 HTML/JS (SockJS + STOMP.js CDN)
- **언어**: 코드 주석 및 UI 텍스트는 한국어

### 인프라 (docker-compose.yml)

| 서비스     | 포트 | 용도                                          |
|------------|------|-----------------------------------------------|
| Kafka      | 9092 | 메시지 브로커 (컨슈머 그룹: `chat-group`)     |
| Zookeeper  | 2181 | Kafka 코디네이션                              |
| Redis      | 6379 | 설정되어 있으나 아직 미사용                   |
| PostgreSQL | 5432 | R2DBC 설정 완료 (`chatdb`), 아직 미사용       |

### 패키지 구조 (`com.allpick.reactivechat`)

- `config/` — WebSocket, Kafka, Jackson 설정
- `controller/` — WebSocket 메시지 핸들러 + REST 엔드포인트
- `kafka/` — 채팅 메시지 Kafka 프로듀서/컨슈머
- `model/` — `ChatMessage` (`MessageType` enum: CHAT, JOIN, LEAVE) 및 DTO
- `service/` — `OnlineUserService` (인메모리 유저 추적)
