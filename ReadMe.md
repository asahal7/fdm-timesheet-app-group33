# FDM Timesheet System

> A full-stack timesheet management application built for FDM Group, enabling consultants to submit weekly timesheets and managers to review, approve, or reject them — with finance reporting and a full audit trail.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Database Setup](#database-setup)
  - [Backend](#backend)
  - [Frontend](#frontend)
- [Usage](#usage)
  - [User Roles](#user-roles)
  - [Timesheet Lifecycle](#timesheet-lifecycle)
- [API Reference](#api-reference)
- [Contributors](#contributors)

---

## Overview

The FDM Timesheet System provides a structured, role-based workflow for managing employee timesheets from creation through to payroll. Consultants log their weekly hours, managers approve or reject submissions with feedback, and the finance team accesses approved records with CSV export for payroll processing.

---

## Features

| Feature | Description |
|---|---|
| **Role-Based Access** | Three distinct roles — Consultant, Manager, Finance — each with scoped permissions |
| **Timesheet Workflow** | Draft → Pending Approval → Approved / Rejected lifecycle |
| **Comments & Feedback** | Consultants add submission notes; managers provide rejection feedback |
| **Resubmission** | Rejected timesheets unlock for editing and can be resubmitted |
| **Finance Reporting** | Search and filter approved timesheets; export to CSV for payroll |
| **Audit Trail** | Every action (created, submitted, approved, rejected, resubmitted) is logged with actor ID and timestamp |
| **Validation** | Hours per day (0–24), no duplicate days, max 7 entries per week, locked timesheets cannot be edited |

---

## Tech Stack

**Frontend**
- React 19 + TypeScript
- Vite
- Material-UI (MUI v7) with custom FDM dark theme
- Axios

**Backend**
- Java 21 + Spring Boot 3.3.2
- Spring Data JPA + Hibernate
- Spring Validation
- Maven

**Database**
- PostgreSQL

---

## Project Structure

```
fdm-timesheet-app-group33/
├── frontend/
│   └── src/
│       ├── api/              # Axios API client
│       ├── components/       # React components (TimesheetList, TimesheetDetail, FinanceView, ...)
│       ├── types/            # Shared TypeScript interfaces
│       ├── theme.ts          # Custom MUI theme (FDM branding)
│       └── App.tsx           # Root component & role selection
│
└── backend/
    └── src/main/java/com/group33/timesheet/
        ├── controller/       # REST endpoints
        ├── service/          # Business logic & validation
        ├── domain/           # JPA entities & enums
        ├── dto/              # Request/response DTOs
        ├── repository/       # Spring Data JPA repositories
        └── exception/        # Global exception handling
```

---

## Getting Started

### Prerequisites

- **Node.js** v18+ and npm
- **Java 21** (JDK)
- **Maven** 3.8+
- **PostgreSQL** running locally

---

### Database Setup

Create a PostgreSQL database:

```bash
createdb timesheet_db
```

Update the credentials in `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/timesheet_db
    username: YOUR_POSTGRES_USER
    password: YOUR_POSTGRES_PASSWORD
```

The schema is auto-generated on startup via Hibernate (`ddl-auto: create-drop`). No migrations needed.

---

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

The Vite dev server proxies all `/timesheets` requests to `http://localhost:8080`, so no CORS configuration is needed during development.

---

## Usage

### User Roles

On launch, select your role and enter your Employee ID. You can switch roles at any time.

| Role | Permissions |
|---|---|
| **Consultant** | Create timesheets, add daily entries, submit for approval, add comments, resubmit rejected timesheets |
| **Manager** | View assigned timesheets, approve or reject with comments |
| **Finance** | View approved timesheets only, filter by consultant/manager/date range, export to CSV |

> Authentication is header-based (`X-User-Role`, `X-Consultant-Id`) — suitable for a development/demo context.

---

### Timesheet Lifecycle

```
DRAFT  ──► PENDING_APPROVAL  ──► APPROVED
                │
                └──► REJECTED  ──► (resubmit) ──► PENDING_APPROVAL
```

1. **Consultant** creates a timesheet and adds daily hour entries (up to 7, one per day).
2. **Consultant** submits the timesheet, optionally with a comment. Status moves to `PENDING_APPROVAL`.
3. **Manager** reviews and either:
   - **Approves** — timesheet is locked and visible to finance.
   - **Rejects** — manager must provide a reason; timesheet unlocks for editing.
4. **Consultant** can update and resubmit a rejected timesheet.
5. **Finance** searches and exports all approved timesheets.

---

## API Reference

All endpoints are prefixed with `/timesheets`. Role is passed via the `X-User-Role` header; consultant/manager identity via `X-Consultant-Id`.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/timesheets` | Create a new timesheet |
| `GET` | `/timesheets` | List all timesheets |
| `GET` | `/timesheets/{id}` | Get a single timesheet |
| `POST` | `/timesheets/{id}/entries` | Add a daily entry |
| `POST` | `/timesheets/{id}/submit` | Submit for manager approval |
| `POST` | `/timesheets/{id}/resubmit` | Resubmit after rejection |
| `POST` | `/timesheets/{id}/approve` | Approve a timesheet (Manager) |
| `POST` | `/timesheets/{id}/reject` | Reject with feedback (Manager) |
| `GET` | `/timesheets/finance` | Get approved timesheets (Finance) |
| `GET` | `/timesheets/finance/export` | Export approved timesheets as CSV (Finance) |

---

## Contributors

**Group 33** — [QMUL Repository](https://github.qmul.ac.uk/ex23302/fdm-timesheet-app-group33)

| Name | GitHub |
|---|---|
| Abdimaalik Sahal | [@asahal7](https://github.qmul.ac.uk/asahal7) |
| Arin Kaptan | [@arinkaptan](https://github.qmul.ac.uk/arinkaptan) |
| Jonathan Ibekwe | [@Jonathan-Ndukauba-Ibekwe](https://github.qmul.ac.uk/Jonathan-Ndukauba-Ibekwe) |
| Aatif Saumtally | [@aatifsaumtally](https://github.qmul.ac.uk/aatifsaumtally) |
| Hassan Al Jabir | [@HassanAl-Jabir](https://github.qmul.ac.uk/HassanAl-Jabir) |
| Sinan | [@Sinanmk07](https://github.qmul.ac.uk/Sinanmk07) |
| Abdul Moiz Khan | [@AbdulMoizKhan06](https://github.qmul.ac.uk/AbdulMoizKhan06) |
