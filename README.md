# runing
> cd environment

> docker compose up -d

> run intellj bình thường

# APSAS
hệ thống Automated Programming Skills Assessment System (APSAS) được đề xuất nhằm tự động hóa việc chấm code, đánh giá kỹ năng và đưa ra phản hồi cá nhân hóa, giúp nâng cao trải nghiệm học tập cho sinh viên và giảm tải công việc cho giảng viên

## 2) Backend Architecture (Monolith-Modular, DDD-oriented)

> Spring Boot 3, Java 17+, mysql, WebClient, JWT, Flyway, Micrometer.

### 2.1 High-Level System Flow

```mermaid
flowchart LR
  subgraph CLIENT
    W[Web App]
    M[Mobile App]
  end

  subgraph BACKEND_MONOLITH
    CTRL[HTTP Controllers]
    IAM[Auth & RBAC]
    CNT[Content]
    ASM[Assignment]
    SUB[Submission/Grader]
    EVL[Evaluation/AI Feedback]
    SKL[Skill Analytics]
    INT[Integration]
    SEC[Security Center]
  end

  subgraph EXTERNAL
    J0[Judge0/Piston]
    SA[SAST/Complexity]
    OBS[Prometheus / ELK]
  end

  subgraph STORAGE
    DB[(MySQL)]
    CACHE[(Redis - optional)]
  end

  W --> CTRL
  M --> CTRL

  CTRL --> IAM & CNT & ASM & SUB & EVL & SKL & INT & SEC

  SUB --> J0
  SUB --> SA
  EVL --> DB
  SKL --> DB
  CNT --> DB
  ASM --> DB
  INT --> DB
  SEC --> DB

  BACKEND_MONOLITH --> OBS
  BACKEND_MONOLITH <--> CACHE
  BACKEND_MONOLITH <--> DB

```

### 2.2 Component Responsibility Map

```mermaid
flowchart LR
  A[Auth/RBAC] -->|JWT/Permissions| G[API Controllers]
  G --> C[Content]
  G --> S[Submission/Grader]
  G --> K[Skill Analytics]
  G --> I[Integration Manager]
  G --> Z[Security Center]

  C --> DB[(mysql)]
  S --> J0[Judge0/Piston]
  S --> ST[SAST/CCN/Style]
  S --> F[AI Feedback]
  F --> DB
  K --> DB
  I --> DB
  Z --> DB
```

### 2.3 Sequence – Grading Pipeline (rút gọn)

```mermaid
sequenceDiagram
  participant U as Client
  participant API as API Controller
  participant Sub as Submission Service
  participant J as Judge0/Piston
  participant SA as SAST/Complexity
  participant AI as AI Feedback
  participant SK as Skill Analytics
  participant DB as MySQL

  U->>API: POST /submissions (code, lang)
  API->>Sub: validate & enqueue
  Sub->>J: run tests
  J-->>Sub: results
  Sub->>SA: analyze(code)
  SA-->>Sub: metrics
  Sub->>AI: build prompt(results+metrics)
  AI-->>Sub: feedback
  Sub->>DB: save submission+scores+feedback
  Sub->>SK: update snapshots
  SK-->>DB: write
  API-->>U: 200 (scores + feedback)

```

---

## 3) Module Structure (source tree)

```text
src/
 └─ main/
     ├─ java/com/example/project/
     │   ├─ controller/        # REST Controllers (API layer)
     │   ├─ service/           # Business logic
     │   │    └─ impl/         # Triển khai service
     │   ├─ repository/        # Repository (Spring Data JPA interface)
     │   ├─ model/             # Entity (JPA @Entity) hoặc DTO
     │   ├─ dto/               # Request/Response objects
     │   ├─ config/            # Cấu hình: Security, CORS, Swagger, Database
     │   └─ exception/         # Custom exception + @ControllerAdvice
     └─ resources/
         ├─ application.yml    # cấu hình app
         └─ db/migration/      # Flyway/Liquibase scripts
```

---
