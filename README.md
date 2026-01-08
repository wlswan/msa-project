# Msa-Board 

**확장성 있는 게시판 서비스**를 목표로 하며, 서비스 간 결합도를 낮추기 위해 **Event-Driven Architecture**를 채택했습니다.
RabbitMQ를 이용한 비동기 데이터 동기화, Redis와 Redisson을 활용한 성능 및 동시성 제어, 그리고 장애 전파 방지를 위한 다양한 패턴(Circuit Breaker, Rate Limiter)을 적용했습니다.

---

## 🛠 기술 스택 (Tech Stack)

**Backend**
- **Framework**: Spring Boot 3.x, Spring Cloud Gateway
- **Security**: Spring Security, JWT (Stateless 인증)
- **ORM**: Spring Data JPA

**Network & Messaging**
- **Message Broker**: RabbitMQ (Event-Driven)

**Database & Cache**
- **RDBMS**: H2, MySQL
- **NoSQL / Cache**: Redis (Caching, Rate Limiting)
- **Concurrency Control**: Redisson (Distributed Lock)

**Realtime**
- **Protocol**: WebSocket (실시간 알림 전송)

**Resilience & Observability**
- **Circuit Breaker**: Resilience4j (장애 격리 및 복구)
- **Monitoring**: Zipkin (Distributed Tracing), Prometheus, Micrometer

**Build Tool**
- **Build**: Gradle, Docker

---

## 📦 주요 기능 (Features)

### 1. Gateway Service (Port: 8080)
- 요청 경로(path)에 따라 적절한 서비스로 라우팅합니다.
- `JwtAuthFilter`를 통해 헤더의 토큰을 검증하여 유효한 사용자만 접근을 허용합니다.
- Redis Token Bucket 알고리즘을 사용하여 클라이언트 IP 또는 User Key 기반으로 초당 요청 수를 제한하여 서버를 보호합니다.

### 2. Auth Service (Port: 8081)
- Access Token 및 Refresh Token 발급, 검증, 재발급을 수행합니다.
- 회원가입, 로그인 로직을 처리합니다.

### 3. Board Service (Port: 8083)
- 게시글 생성 트랜잭션과 이벤트 발행을 원자적으로 처리하기 위해 아웃박스 패턴을 구현했습니다. DB에 이벤트를 먼저 저장하고 별도의 Relay가 이를 발행하여 메시지 유실을 방지합니다.
- 게시글 생성 시 `post.created` 이벤트를 RabbitMQ로 발행합니다.

### 4. Comment Service (Port: 8084)
- 게시글 정보 조회 시 `Resilience4j`를 적용했습니다. Board Service 장애 시 서킷 브레이커가 작동하여 장애 전파를 차단하고, Fallback 메커니즘을 통해 기본값 반환 등의 대체 로직을 수행합니다.
- `Redisson`을 도입하여 캐싱에 동시에 접근할 때 발생하는 동시성 문제를 해결합니다.
- `post.created` 이벤트를 수신하여 필요한 데이터를 비동기로 동기화합니다.
- 댓글 생성 시 `comment.created` 이벤트를 RabbitMQ로 발행합니다.

### 5. Notification Service (Port: 8085)
- 클라이언트와 웹소켓 연결을 통해 실시간으로 알림을 전송합니다.
- 댓글 작성 이벤트(`comment.created`) 발생 시 즉시 해당 게시글 작성자에게 알림을 보냅니다.