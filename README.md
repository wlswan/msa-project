## Msa-Board

### Auth-Service
- 사용자 인증 및 권한 관리 담당
- 로그인, 회원가입, JWT 발급 및 검증 수행
- 다른 서비스 접근 시 인증 토큰 검증 담당

### Board-Service
- 게시글 생성, 수정, 삭제, 조회 담당
- 게시글 생성 시 `post.created` 게시글 이벤트 발행
- Comment-Service와 RabbitMQ로 연동 (postId,author)

### Comment-Service
- 댓글 및 대댓글 CRUD 담당
- (postId, author) 게시글 생성 이벤트 수신 및 Redis에 캐싱 or OpenFeign 통신으로 정보 불러옴
- 댓글 작성 시 `comment.created` 댓글 이벤트 발행

### Notification-Service
- 댓글 생성 이벤트 수신 및 알림 생성
- WebSocket을 통한 실시간 알림 전송
- 사용자 알림 데이터 저장 및 조회 담당

### Gateway-Service
- 모든 요청의 진입점 역할 수행
- 서비스 라우팅 및 JWT 인증 처리
- 각 서비스로 요청을 전달하는 게이트웨이 역할  
