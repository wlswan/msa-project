# MSA Board

Event-Driven Architecture 기반의 마이크로서비스 게시판 시스템

## 아키텍처

```
                            ┌─────────────────┐
                            │  Gateway (8080) │
                            │  JWT + Rate Limit│
                            └────────┬────────┘
                                     │
        ┌────────────┬───────────────┼───────────────┬────────────┐
        ▼            ▼               ▼               ▼            ▼
   ┌─────────┐  ┌─────────┐    ┌─────────┐    ┌─────────┐   ┌─────────┐
   │  Auth   │  │  Board  │    │ Comment │    │ Notifi- │   │   WS    │
   │  8081   │  │  8083   │    │  8084   │    │ cation  │   │  /ws    │
   └─────────┘  └────┬────┘    └────┬────┘    │  8085   │   └─────────┘
                     │              │         └────┬────┘
                     │   RabbitMQ   │              │
                     ▼              ▼              ▼
              ┌──────────────────────────────────────────┐
              │           post.created                   │
              │           comment.created                │
              └──────────────────────────────────────────┘
```

## 기술 스택

- **Java 21**, Spring Boot 3.x
- **RabbitMQ** - 이벤트 기반 비동기 통신
- **Redis** - 캐싱, Rate Limiting, 분산 락
- **WebSocket + STOMP** - 실시간 알림
- **Resilience4j** - Circuit Breaker
- **Zipkin + Prometheus** - 분산 추적, 모니터링

## 주요 패턴

### 1. Outbox Pattern
메시지 유실 방지를 위해 이벤트를 DB에 먼저 저장 후 Relay가 발행

```
트랜잭션 {
  1. 데이터 저장
  2. Outbox 테이블에 이벤트 저장
}
↓
Relay (5초 주기) → RabbitMQ 발행 → 상태 SENT로 변경
```

### 2. 분산 락 (Redisson)
캐시 갱신 시 동시성 제어

### 3. Circuit Breaker
Board Service 장애 시 Comment Service로 전파 차단
- 실패율 50% 초과 시 서킷 오픈
- 10초 후 Half-Open 상태로 복구 시도

## 이벤트 흐름

```
[게시글 생성]
Board → post.created → Comment (게시글 정보 캐싱)

[댓글 생성]
Comment → comment.created → Notification → WebSocket → 클라이언트
```
## 서비스 구성

| 서비스 | 포트 | 역할 |
|--------|------|------|
| Gateway | 8080 | JWT 인증, Rate Limiting, 라우팅 |
| Auth | 8081 | 회원가입, 로그인, 토큰 발급/갱신 |
| Board | 8083 | 게시글 CRUD, `post.created` 이벤트 발행 |
| Comment | 8084 | 댓글 CRUD, `comment.created` 이벤트 발행 |
| Notification | 8085 | WebSocket 실시간 알림 전송 |

## API 엔드포인트

### Auth
| Method | Path | 설명 |
|--------|------|------|
| POST | /auth/signup | 회원가입 |
| POST | /auth/login | 로그인 |
| POST | /auth/refresh | 토큰 갱신 |

### Board
| Method | Path | 설명 |
|--------|------|------|
| POST | /posts | 게시글 생성 |
| GET | /posts | 게시글 목록 |
| GET | /posts/{id} | 게시글 상세 |
| PUT | /posts/{id} | 게시글 수정 |
| DELETE | /posts/{id} | 게시글 삭제 |

### Comment
| Method | Path | 설명 |
|--------|------|------|
| POST | /posts/{postId}/comments | 댓글 생성 |
| GET | /posts/{postId}/comments | 댓글 목록 |
| PUT | /posts/{postId}/comments/{id} | 댓글 수정 |
| DELETE | /posts/{postId}/comments/{id} | 댓글 삭제 |

### Notification
| Method | Path | 설명 |
|--------|------|------|
| GET | /notifications | 알림 목록 |
| WebSocket | /ws | 실시간 알림 연결 |


