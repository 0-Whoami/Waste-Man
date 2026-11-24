# CleanTrack Architecture & ERD Diagrams

## System Architecture Diagram
```mermaid
flowchart TD
    subgraph Frontend [Browser (HTML/JS/Tailwind)]
        RP[Resident Portal]
        AD[Admin Dashboard]
        MAP[Leaflet Map]
        CHART[Chart.js Analytics]
    end

    subgraph Backend [FastAPI Backend]
        API_PUB[Public Routes]
        API_ADM[Admin Routes]
        AUTH[Auth/Session Manager]
        SVC[Business Logic / Hotspot Engine]
    end

    subgraph Storage [Local Storage]
        DB[(SQLite Database)]
        FS[File System /static/uploads/]
    end

    RP -->|POST /api/complaints| API_PUB
    RP -->|GET /api/complaints/{id}| API_PUB
    RP -->|GET /api/stats/public| API_PUB
    
    AD -->|POST /api/auth/login| AUTH
    AUTH -.->|Signed Cookie| AD
    AD -->|GET /PATCH /api/admin/*| API_ADM
    
    API_PUB --> SVC
    API_ADM --> SVC
    
    SVC -->|Validate & Query| DB
    SVC -->|Save Images| FS
    
    API_ADM -->|Fetch Data| MAP
    API_ADM -->|Fetch Data| CHART
```

## Entity Relationship Diagram (ERD)
```mermaid
erDiagram
    USERS {
        INTEGER id PK
        TEXT name
        TEXT email UK
        TEXT password_hash
        TEXT role "resident, admin"
        TIMESTAMP created_at
    }

    AREAS {
        INTEGER id PK
        TEXT area_name UK
        TEXT ward_code
        REAL latitude
        REAL longitude
    }

    COMPLAINTS {
        INTEGER id PK
        TEXT tracking_code UK
        TEXT reporter_name
        TEXT reporter_contact
        INTEGER user_id FK "nullable"
        INTEGER area_id FK
        TEXT category
        TEXT description
        TEXT image_url "nullable"
        TEXT priority "Low, Medium, High"
        INTEGER is_hotspot_escalated
        TEXT status "Submitted, In Progress, Resolved"
        TEXT assigned_team
        TIMESTAMP created_at
        TIMESTAMP resolved_at "nullable"
    }

    STATUS_LOGS {
        INTEGER id PK
        INTEGER complaint_id FK
        INTEGER updated_by FK "nullable"
        TEXT old_status "nullable"
        TEXT new_status
        TEXT remark
        TIMESTAMP updated_at
    }

    USERS ||--o{ COMPLAINTS : "reports (optional)"
    USERS ||--o{ STATUS_LOGS : "updates as admin"
    AREAS ||--o{ COMPLAINTS : "contains"
    COMPLAINTS ||--o{ STATUS_LOGS : "has history"
```
