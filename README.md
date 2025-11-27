# APSAS - Automated Programming Skills Assessment System

Hệ thống tự động chấm code, đánh giá kỹ năng và đưa ra phản hồi cá nhân hóa, giúp nâng cao trải nghiệm học tập cho sinh viên và giảm tải công việc cho giảng viên.

## 🚀 Quick Start

### 1. Start Docker Services
```bash
cd environment
docker compose up -d
```

### 2. Run Application

**Cách 1: Normal Start (Quick)**
```powershell
.\run-local.ps1
```

**Cách 2: Start với Cleanup** (nếu gặp lỗi)
```powershell
.\run-local.ps1 -Cleanup
```

**Cách 3: Force Cleanup** (không hỏi)
```powershell
.\run-local.ps1 -Cleanup -Force
```

📖 **Chi tiết:** Xem [STARTUP_GUIDE.md](STARTUP_GUIDE.md)

---


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
     ├─ java/com/project/apsas/
     │    ├─ configuration/    # Cấu hình hệ thống (Security, CORS, Swagger, Database...)
     │    ├─ controller/       # REST Controllers (API layer - Điểm tiếp nhận request)
     │    ├─ dto/              # Data Transfer Objects (Request/Response models)
     │    ├─ entity/           # JPA Entities (Ánh xạ bảng Database)
     │    ├─ enums/            # Các hằng số định danh (Status, Type, Roles...)
     │    ├─ exception/        # Xử lý lỗi tập trung (@ControllerAdvice, Custom Exceptions)
     │    ├─ integration/      # Xử lý tích hợp bên ngoài (Judge0, AI Service, Email...)
     │    ├─ mapper/           # Chuyển đổi dữ liệu giữa Entity và DTO (MapStruct)
     │    ├─ repository/       # Data Access Layer (Spring Data JPA)
     │    ├─ service/          # Business Logic (Xử lý nghiệp vụ chính)
     │    └─ ApsasApplication  # Class khởi chạy ứng dụng (Main class)
     └─ resources/
          └─ application.yaml  # File cấu hình chính của ứng dụng
```

---
