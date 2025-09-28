# APSAS
hệ thống Automated Programming Skills Assessment System (APSAS) được đề xuất nhằm tự động hóa việc chấm code, đánh giá kỹ năng và đưa ra phản hồi cá nhân hóa, giúp nâng cao trải nghiệm học tập cho sinh viên và giảm tải công việc cho giảng viên
## 2) Backend Architecture (Monolith‑Modular, DDD‑oriented)

> Spring Boot 3, Java 17+, PostgreSQL, WebClient, JWT, Flyway, Micrometer. Một app duy nhất nhưng chia **bounded contexts** (có thể tách thành microservices sau).

## 2) Backend Architecture (Monolith-Modular, DDD-oriented)

> Spring Boot 3, Java 17+, PostgreSQL, WebClient, JWT, Flyway, Micrometer.

### 2.1 High-Level System Flow

```mermaid
flowchart LR
  subgraph CLIENT
    W[Web App]
    M[Mobile App]
    W --- M
  end

  subgraph BACKEND_MONOLITH
    CTRL[HTTP Controllers]
    AUTH[Auth & RBAC]
    CNT[Content Service]
    ASM[Assignment Service]
    SUB[Submission / Grader]
    AFL[AI Feedback]
    ANA[Skill Analytics]
    INT[Integration Manager]
    SEC[Security Center - Hot Reload]

    CTRL --> AUTH
    CTRL --> CNT
    CTRL --> ASM
    CTRL --> SUB
    CTRL --> AFL
    CTRL --> ANA
    CTRL --> INT
    CTRL --> SEC
  end

  subgraph EXTERNAL_SERVICES
    J0[Online Judge: Judge0 / Piston]
    SA[SAST & Complexity]
    OBS[Observability: Prometheus / ELK]
  end

  subgraph STORAGE
    DB[(mysql)]
    CACHE[(Redis - optional)]
  end

  W --> CTRL
  M --> CTRL

  SUB --> J0
  SUB --> SA
  SUB --> AFL --> DB
  SUB --> ANA --> DB

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
  participant DB as mysql

  U->>API: POST /submissions (code, lang)
  API->>Sub: validate & enqueue
  Sub->>J: run tests (per-case)
  J-->>Sub: results
  Sub->>SA: analyze(code)
  SA-->>Sub: metrics (CCN, smells)
  Sub->>AI: build prompt(results+metrics)
  AI-->>Sub: feedback (text)
  Sub->>DB: save submission+scores+feedback
  Sub->>SK: update EMA/decay snapshots
  SK-->>DB: write snapshots
  API-->>U: 200 (scores + feedback)
```

---

## 3) Module Structure (source tree)

```text
src/main/java/com/apsas/
├─ common/                 # DTO, exception, mapper, utils, web handlers
├─ config/                 # OpenAPI, WebClient, Jackson, CORS (hot)
├─ security/               # JWT + RBAC
├─ auth/                   # register/login/refresh
├─ content/                # content + version + review/publish
├─ assignment/             # assignment + deadline + rubric
├─ submission/             # grading orchestrator + judge clients + analysis
├─ feedback/               # AI feedback
├─ skills/                 # EMA/decay + snapshots + APIs
├─ integration/            # connectors + logs + test
├─ secconfig/              # security policy center (hot-reload)
├─ sysconfig/              # system change → CI/CD
└─ observability/          # links/proxy to dashboards
```

---
