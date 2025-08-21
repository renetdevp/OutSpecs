# OutSpecs

# 📁 프로젝트 개요

- 개발자 통합 커뮤니티 (게시글 · 오픈프로필 · 1:1 채팅 · AI 챗봇)
- Spring Boot 기반, STOMP WebSocket, OAuth2(Google), AWS S3 연동
- 프로젝트 기간 : 2025.07.29 ~ 2025.08.25

# 📺️ 프로젝트 시연 영상
- [영상 링크]()

# 🤝 팀 소개
<table border= 1px solid align="center">
  <thead>
  <tr><td colspan=4 align="center">99퍼센트</td></tr>
  </thead>
  <tr align="center">
    <td>팀장 신동규</td>
    <td>팀원 이수윤</td>
    <td>팀원 최수호</td>    
  </tr>
  <tr>
    <td><a href=https://github.com/renetdevp><img object-fit=fill src=https://avatars.githubusercontent.com/u/20268350?v=4 width="200" height="200" alt="깃허브 페이지 바로가기"></a></td>
    <td><a href=https://github.com/suyunlee><img object-fit=fill src=https://avatars.githubusercontent.com/u/87362279?v=4 width="200" height="200" alt="깃허브 페이지 바로가기"></a></td>
    <td><a href=https://github.com/Hasegos><img object-fit=fill src=https://avatars.githubusercontent.com/u/93961708?v=4 width="200" height="200" alt="깃허브 페이지 바로가기"></a></td>    
  </tr>
</table>

# 🛠️ 기술 스택
- **Frontend**: <img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white"> <img src="https://img.shields.io/badge/css-1572B6?style=for-the-badge&logo=css3&logoColor=white"> <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=JavaScript&logoColor=black">
- **Backend**: <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
- **Database**: <img src="https://img.shields.io/badge/postgresql-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"> <img src="https://img.shields.io/badge/h2Database-09476B?style=for-the-badge&logo=H2Database&logoColor=white">
- **Hosting** : <img src="https://img.shields.io/badge/amazonaws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
- **Tooling**: <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=Figma&logoColor=white"> <img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"> <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white">


# 📁 디렉토리 구조

  ```
OutSpecs/
├── 📦 com.percent99.OutSpecs/
│   ├── ⚙️ Config/            # 설정 클래스 (Security, Web 등)
│   ├── 🧭 controller/        # 웹 요청 처리 컨트롤러
│   ├── 📨 dto/               # DTO (데이터 전송 객체)
│   ├── ❌ handler/           # 예외 처리 및 service handler
│   ├── 🧩 entity/            # JPA 엔티티 및 도메인 모델
│   ├── 🗂️ repository/        # JPA 리포지토리
│   ├── 🔐 security/          # 인증/인가 로직
│   ├── 🧠 service/           # 비즈니스 로직 처리 
│   ├── 📁 util/              # 유틸리티 및 커스텀 검증
│   └── 🚀 OutSpecsApplication.java  # 메인 실행 클래스

├── 📁 resources/
│   ├── 🌐 static/            # 정적 자원 (브라우저에서 직접 접근)
│   │   ├── 🎨 css/           # 스타일시트
│   │   ├── 🖼️ images/        # 이미지 파일
│   │   └── ⚙️ js/            # 자바스크립트
│   └── 📄 templates/         # Thymeleaf 템플릿
│       ├── 🔐 admin/          # 유저 관리 관련 뷰
│       ├── 🔐 auth/          # 로그인/회원가입 등 인증 관련 뷰
│       ├── 💬 chat/          # 채팅방 관련 뷰
│       ├── ❌ error/         # 에러 페이지
│       ├── 🧩 fragments/     # 공통 레이아웃 (header/footer 등)
│       ├── 📝 post/          # 게시글 관련 뷰
│       ├── 📝 profile/          # 오픈프로필 관련 뷰
│       ├── 📝 search/          # 검색페이지 관련 뷰
│       └── 🏠 home.html       # 홈 화면 뷰 (루트 페이지)
│   └─── 📄 application.yml         # 공통 설정
├─ 📄 .env # 중요/개인 정보 작성
```


# 📊 ERD (Entity Relationship Diagram)

## 🗺️ ERD 이미지

<img width="1516" height="484" alt="Image" src="https://github.com/user-attachments/assets/66ef522c-b12a-43ba-8012-9cd42ef03a6e" />

# 🗏 페이지 구성

### 메인 페이지

<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/76a382cf-9e36-4345-a864-fd3b8c2eef63" />

<br>

- 메인 담당자 : 최수호
- 주요 개발 기능 : 페이지 flex 및 반응형 구성, 헤더 (공통 페이지) 구현

---

### 로그인 / 회원 가입 페이지

<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/bf205667-6873-4061-af08-11e2cc06a8ea" />
<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/6ada0393-1962-4e21-84db-767f909cabc6" />

<br>

- 메인 담당자 : 최수호
- 주요 개발 기능
    + 로그인
    + 가입 폼 처리
    + FilterChain을 사용한 인증
    + 인가 처리
    + 소셜 로그인 (구글) 연동
    + 비밀번호 암호화(BCrypt)·정책 검증
    + CSRF 보호·세션 고정 방지
    + 중복 검사(아이디·닉네임) 및 Bean Validation
    + 로그아웃 핸들러(쿠키/세션 정리)

---

### 오픈프로필 페이지

<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/5e1d048e-a449-4d82-bdd1-745bbe8d9e38" />
<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/698267dc-4bd4-4423-8717-c6ca9ca54397" />
<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/9e57e535-abba-493b-b504-2e48a38636fc" />

<br>

- 메인 담당자 : 최수호
- 주요 개발 기능
    + 오픈프로필 CRUD(자기소개·경력·스택 태그)
    + 프로필 이미지 업로드·크롭(드래그/휠·5MB 제한)
    + 기업 접근 허용 토글(공개 범위)
    + 탭형 피드(팔로우·좋아요·북마크·알림)
    + 권한 분기(SELF 수정/삭제·ALL 열람)
    + S3 연동(기존 이미지 교체/삭제)

---

### 게시물 페이지 (리스트 / 등록 / 상세 페이지)

- 메인 담당자 :
- 주요 개발 기능 :

---

### 검색 페이지

<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/b890fec4-0522-4233-a627-499306c9f0e4" />

<br>

- 메인 담당자 : 최수호
- 주요 개발 기능
    + 복합 검색(게시판 유형·키워드)
    + 카테고리 필터·결과 배치
    + 인기 Top10 우선+최신순 정렬

---

### 관리자 페이지

<img width="400" height="900" alt="Image" src="https://github.com/user-attachments/assets/b161e644-ea75-4205-af44-7198fc80496d" />

<br>

- 메인 담당자 : 최수호
- 주요 개발 기능
    + 회원 목록/검색/정렬(이메일·권한·가입일)
    + 권한 변경 드롭다운 적용 (USER/ENTUSER)
    + 회원 상태 제어: 정지/해제/탈퇴 (세션/토큰 무효화 처리)
    + 신고 게시글 관리: 목록 확인, 보기/삭제
    + 접근 제어: ROLE_ADMIN, @PreAuthorize 적용, CSRF 보호

---

### 채팅 페이지


- 메인 담당자 :
- 주요 개발 기능 :


# 📌 URL 명세표

## 공통 권한 라벨

`ALL`: 누구나, `USER`: 로그인 필요, `SELF`: 본인 리소스(로그인+경로의 식별자가 본인), `ADMIN`: 관리자

### 👤 유저(User)

<details>
<summary>엔드포인트 상세 보기</summary>

- `POST /users/signup` : `{ username, password }` 생성된 유저 정보 반환

- `GET /users/signup` : 회원가입페이지 반환

- `GET /users/login` : 유저 로그인 페이지 반환

- `POST /login` : {username, password }유저 로그인 정보 반환
</details>

<br>

| Method | Path            | 권한  | 비고                                  |
| ------ | --------------- | --- | ----------------------------------- |
| POST   | `/users/signup` | ALL | 회원가입 처리                             |
| GET    | `/users/signup` | ALL | 회원가입 페이지 *(오타 수정)*                  |
| GET    | `/users/login`  | ALL | 로그인 페이지                             |
| POST   | `/login`        | ALL | 로그인 처리 *(Spring Security 기본 엔드포인트)* |

<br>

### 오픈프로필 (Profile)

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /users/profiles` : 현재 로그인 사용자 프로필 조회 (만약 없으면 → `profile` 뷰 반환)

- `GET /users/profiles/new` : 프로필 생성 폼(`profile`) 뷰 반환 (이미 존재 시 상세로 리다이렉트)

- `POST /users/profiles` : `{nickname, stacks, experience, allowCompanyAccess}` + 이미지 파일 업로드 → 생성 후 상세로 리다이렉트

- `GET /users/profiles/{userId}` : 주어진 userId 프로필 조회 → `profile_detail` 뷰 반환 (없으면 생성 폼으로 리다이렉트)

- `GET /users/profiles/{userId}/edit` : 본인 프로필 수정 폼(`profile`) 뷰 반환 (본인이 아닐 경우 상세로 리다이렉트)

- `POST /users/profiles/{userId}` : `{nickname?, stacks?, experience?, allowCompanyAccess?}` + 파일 업로드 → 수정 후 상세로 리다이렉트

- `POST /users/profiles/{userId}/delete` : 본인 프로필 삭제 → 새로운 폼 생성(`/users/profiles/new`)으로 리다이렉트

- `POST /users/profiles/{userId}/follow` : 로그인한 사용자가 다른 사용자를 팔로우

- `POST /users/profiles/{userId}/like/{postId}/delete` : 사용자가 좋아요한 게시글 삭제

- `POST /users/profiles/{userId}/follow/delete` : 사용자가 팔로우한 유저 삭제

- `POST /users/profiles/{userId}/bookmark/{postId}/delete` : 사용자가 북마크한 게시글 삭제
</details>

<br>

| Method | Path                                                | 권한   | 비고                    |
| ------ | --------------------------------------------------- | ---- | --------------------- |
| GET    | `/users/profiles`                                   | USER | 내 프로필 조회 |
| GET    | `/users/profiles/new`                               | USER | 생성 폼 |
| POST   | `/users/profiles`                                   | USER | 생성 |
| GET    | `/users/profiles/{userId}`                          | ALL  | 프로필 상세 |
| GET    | `/users/profiles/{userId}/edit`                     | SELF | 수정 폼 |
| POST   | `/users/profiles/{userId}`                          | SELF | 수정 |
| POST   | `/users/profiles/{userId}/delete`                   | SELF | 삭제 |
| POST   | `/users/profiles/{userId}/follow`                   | USER | 팔로우  |
| POST   | `/users/profiles/{userId}/follow/delete`            | SELF | 언팔로우 |
| POST   | `/users/profiles/{userId}/like/{postId}/delete`     | SELF | 좋아요 취소 |
| POST   | `/users/profiles/{userId}/bookmark/{postId}/delete` | SELF | 북마크 취소 |

<br>

### 🔐 FORM 로그인

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /users/login` : 커스텀 로그인 페이지 조회

- `POST /login` : 회원가입한 유저인지 확인 →  로그인

- `GET /users/login` : FORM 로그인 실패시 설정된 로그인 페이지로 리다이렉트
</details>
<br>

| Method | Path           | 권한  | 비고                                   |
| ------ | -------------- | --- | ------------------------------------ |
| GET    | `/users/login` | ALL | 로그인 페이지                              |
| POST   | `/login`       | ALL | 로그인 처리 (실패 시 `/users/login` 리다이렉트) |

<br>

### 🔗 OAuth2 로그인

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /users/login`: 커스텀 로그인 페이지 조회

- `GET /oauth2/authorization/google` : 구글 OAuth2 인증 요청 트리거 (버튼 클릭 시 이동)

- `GET /login/oauth2/code/google` : 구글 인증 완료 후 Spring Security 가 인증 코드 처리 및 로그인

- `GET /users/login` : OAuth2 인증 실패 시 설정된 로그인 페이지로 리다이렉트
</details>
<br>

| Method | Path                           | 권한  | 비고              |
| ------ | ------------------------------ | --- | --------------- |
| GET    | `/users/login`                 | ALL | 로그인 페이지         |
| GET    | `/oauth2/authorization/google` | ALL | 구글 OAuth2 시작    |
| GET    | `/login/oauth2/code/google`    | ALL | 콜백(스프링 시큐리티 처리) |

<br>

### 🛡️ 관리자(Admin)

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /admin` : 관리자 페이지 조회

- `POST /admin/posts/{postId}/delete` : 신고가된 게시글 삭제

- `POST /admin/users/{userId}/role` : `{ role }` 변경된 회원 정보 반환

- `POST /admin/users/{userId}/ban` : `{ userId }` 에 대한 회원 정지

- `POST /admin/users/{userId}/unban`  : `{ userId }` 에 대한 회원 정지해제

- `POST /admin/users/{userId}/delete` : `{ userId }` 에 대한 회원 탈퇴
</details>
<br>

| Method | Path                           | 권한    | 비고 |
| ------ | ------------------------------ | ----- | ------------ |
| GET    | `/admin`                       | ADMIN | 관리자 페이지|
| POST   | `/admin/posts/{postId}/delete` | ADMIN | 게시글 삭제  |
| POST   | `/admin/users/{userId}/role`   | ADMIN | 권한 변경 `{role}` |
| POST   | `/admin/users/{userId}/ban`    | ADMIN | 계정 정지 |
| POST   | `/admin/users/{userId}/unban`  | ADMIN | 정지 해제 |
| POST   | `/admin/users/{userId}/delete` | ADMIN | 회원 탈퇴 |

<br>

### 📝 게시글

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /post/write` : 자유, qna, 채용공고, 팀모집, 나가서 놀기 게시글 등록 폼

- `POST /post/write` : 자유, qna, 채용공고, 팀모집, 나가서 놀기 게시글 등록

- `POST /post/{postId}` : 게시글 + 댓글 리스트 반환

- `GET /post/{postId}/edit` : 수정할 게시글의 현재 정보 반환

- `POST /post/{postId}/edit` : 수정된 게시글 반환

- `POST /post/{postId}/delete` : 삭제 성공 여부 반환

- `GET /post/{postId}/comment` : 댓글, 대댓글, 답변 등록

- `POST /post/{postId}/comment/{commentId}` : 댓글, 대댓글, 답변 삭제

- `POST /post/{postId}/comment/{commentId}/edit` : 댓글, 대댓글, 답변 수정

- `POST /post/{postId}/like` : 게시글 좋아요 후 알림 반환

- `POST /post/{postId}/bookmark` : 게시글 북마크 후 알림 반환

- `POST /post/{postId}/report` : 게시글 신고 후 알림 반환

- `POST /post/{postId}/team` : 팀모집 게시글의 참여 신청 등록
</details>
<br>

| Method   | Path                                      | 권한   | 비고 |
| -------- | ----------------------------------------- | ---- | ------- |
| GET      | `/post/write`                             | USER | 등록 폼 |
| POST     | `/post/write`                             | USER | 등록 |
| POST     | `/post/{postId}`                          | ALL  | 상세 조회 |
| GET      | `/post/{postId}/edit`                     | SELF | 수정 폼 |
| POST     | `/post/{postId}/edit`                     | SELF | 수정  |
| POST     | `/post/{postId}/delete`                   | SELF | 삭제  |
| GET     | `/post/{postId}/comment`                  | USER | 댓글/대댓글/답변 등록  |
| POST     | `/post/{postId}/comment/{commentId}`      | SELF | 댓글 삭제 |
| POST     | `/post/{postId}/comment/{commentId}/edit` | SELF | 댓글 수정 |
| POST     | `/post/{postId}/like`                     | USER | 좋아요 |
| POST     | `/post/{postId}/bookmark`                 | USER | 북마크 |
| POST     | `/post/{postId}/report`                   | USER | 신고 |
| POST     | `/post/{postId}/team`                     | USER | 팀모집 참여 신청 |

<br>

### 🗂️ 게시판

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /list/free` : 자유 게시판의 인기 게시글 Top10과 게시글을 최신순으로 표출

- `GET /list/qna` : QnA 게시판의 인기 게시글 Top10과 게시글을 최신순으로 표출

- `GET /list/team` : 팀모집 게시판의 인기 게시글 Top10과 게시글을 최신순으로 표출

- `GET /list/recruit` : 채용공고 게시판의 인기 게시글 Top10과 게시글을 최신순으로 표출

- `GET /list/play` : 나가서놀기 게시판의 인기 게시글 Top10과 게시글을 최신순으로 표출

- `GET /list/free/filter` : 자유 게시글 태그별로 필터링

- `GET /list/qna/filter` : QnA 게시글 태그별로 필터링

- `GET /list/recruit/filter` : 채용공고 게시글 태그별로 필터링

- `GET /list/play/filter` : 나가서놀기 게시글 태그별로 필터링
</details>
<br>

| Method | Path                  | 권한  | 비고        |
| ------ | --------------------- | --- | ------------- |
| GET    | `/list/{type}`        | ALL | `{type}`= "free, qna, team, recruit, play" 인기 Top10 + 최신순 |
| GET    | `/list/{type}/filter` | ALL | `{type}`= "free, qna, recruit, play" 게시글 태그 필터링 |

<br>

### 💬 채팅(Chat)

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /chats` : 사용자의 채팅방 페이지,사용자가 참가하고 있는 모든 채팅방을 해당 채팅방에서 볼 수 있음,  profile이 있어야 함

- `POST /chats` : 채팅방 생성, 채팅을 하고자 하는 쌍방이 모두 profile이 있어야 함, form-data * targetId: 채팅을 하고자 하는 상대방의 userId

- `GET /chats/{chatRoomId}` : 채팅방 정보 반환, 사용자가 해당 채팅방에 참가하고 있어야 함

- `GET /chats/{chatRoomId}/messages` : 채팅방의 메시지 조회, 무한 스크롤을 위해 아직 불러오지 않은 채팅 메시지를 최대 15개를 조회해 반환<br>
  parameter, firstCreatedAt: 이 시각보다 먼저 생성된 채팅 메시지 중 최대 15개의 최근 메시지를 조회함. 기본값은 현재 시각.

- `(SOCKET/publish) /app/chats/{chatRoomId}` : 채팅방에 메시지 전송, body content: 메시지 내용

- `(SOCKET/subscribe) /queue/users/{userId}` : 채팅 채널을 구독, 사용자는 개인 채팅 채널을 가지고 있고, 타인의 채팅 채널을 구독하려 하면 거부함.<br>
    + body
        * chatRoomId: 수신한 메시지가 속한 채팅방의 chatRoomId
        * senderId: 메시지를 송신한 사용자의 userId
        * content: 채팅 메시지의 내용
        * createdAt: 채팅 메시지가 생성된 시각 |
</details>
<br>

| 종류     | Endpoint                       | 권한   | 비고 |
| ------ | ------------------------------ | ---- | ------------- |
| GET    | `/chats`                       | USER | 내 채팅방 목록 (프로필 필요) |
| POST   | `/chats`                       | USER | 채팅방 생성 (양측 프로필 필요) |
| GET    | `/chats/{chatRoomId}`          | USER | 채팅방 정보 (참가자만) |
| GET    | `/chats/{chatRoomId}/messages` | USER | 메시지 페이지네이션 |
| WS/PUB | `/app/chats/{chatRoomId}`      | USER | 메시지 전송 |
| WS/SUB | `/queue/users/{userId}`        | USER | 개인 채널 구독 |

<br>

### 🤖 앨런 AI

<details>
<summary>엔드포인트 상세 보기</summary>

- `GET /chats/alan` : 앨런 AI에게 질의.
    + parameter
        * question: 앨런 AI에게 질문할 내용
        * questionType: 앨런 AI에게 질문할 유형.
            * QUESTION: 일반 질의
            * RECOMMEND: 지역의 명소/맛집 추천 |
</details>
<br>

| Method | Path          | 권한   | 비고                                    |
| ------ | ------------- | ---- | -----------------------------------------  |
| GET    | `/chats/alan` | USER | `params: question, questionType(QUESTION RECOMMEND)` |
