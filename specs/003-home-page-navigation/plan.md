# Implementation Plan: Home Page & Navigation

**Branch**: `003-home-page-navigation` | **Date**: 2026-03-22 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-home-page-navigation/spec.md`

## Summary

Add a home page dashboard and persistent sidebar navigation to the Flowable Platform. The home page displays pending task count, active process count, and a 5-item recent activity feed. A collapsible left sidebar provides navigation to all sections (Home, Tasks, Processes, Forms, Dashboard, Admin) with role-based visibility and mobile-responsive behavior. Login redirects to `/home` instead of `/tasks`.

## Technical Context

**Language/Version**: Java 21, TypeScript 5+
**Primary Dependencies**: Spring Boot 3.5.x, Flowable 7.x, Next.js 14 (App Router), React 18+, Tailwind CSS, Lucide React (icons)
**Storage**: PostgreSQL 15+ (multi-tenant, no schema changes)
**Testing**: JUnit 5 (backend), Jest + React Testing Library (frontend)
**Target Platform**: Linux server (backend), Modern browsers (frontend)
**Project Type**: Web service (multi-tenant workflow platform)
**Performance Goals**: Home page summary loads within 2 seconds, navigation is instant (client-side routing)
**Constraints**: Multi-tenant data isolation, Server/Client Component separation, existing auth/JWT infrastructure
**Scale/Scope**: Frontend-heavy feature with minor backend additions

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [X] **Multi-Tenant Isolation**: Home summary API uses existing tenant-scoped Flowable queries; no cross-tenant data possible
- [X] **Flowable-Native**: Uses TaskService, RuntimeService, HistoryService directly for summary data
- [X] **Server/Client Boundaries**: Sidebar and home page are Client Components for interactivity; data fetched via API routes
- [N/A] **Form-Process Binding**: No forms involved in this feature
- [X] **Test Coverage**: Tests planned for routing, sidebar rendering, home page components, summary API endpoint
- [N/A] **Audit Trail**: No new workflow actions; uses existing audit infrastructure for activity feed
- [X] **Performance**: All Flowable queries use existing tenant-scoped services; summary endpoint uses count queries (not full data)
- [N/A] **Security**: No new sensitive process variables; role-based nav item visibility uses existing RBAC

## Project Structure

### Documentation (this feature)

```text
specs/003-home-page-navigation/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0: Technical decisions
├── data-model.md        # Phase 1: Data models (DTOs, client state)
├── quickstart.md        # Phase 1: Integration guide
├── contracts/           # Phase 1: API contracts
│   ├── home-summary-api.md
│   ├── auth-me-api.md
│   └── login-response-enhanced.md
└── checklists/
    ├── requirements.md
    └── ux.md
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/flowable/platform/
│   ├── controller/
│   │   ├── HomeController.java          # NEW: GET /api/home/summary
│   │   └── AuthController.java          # MODIFY: enhance /me and login response
│   ├── dto/
│   │   ├── HomeSummaryDTO.java          # NEW: summary response DTO
│   │   ├── ActivityItemDTO.java         # NEW: activity feed item DTO
│   │   ├── LoginResponse.java           # MODIFY: add displayName, roles
│   │   └── UserInfoDTO.java             # NEW: /me response DTO
│   └── service/
│       └── HomeSummaryService.java      # NEW: aggregates summary data
└── src/test/java/com/flowable/platform/
    └── test/unit/
        └── HomeSummaryServiceTest.java  # NEW: unit test

frontend/
├── src/
│   ├── app/
│   │   ├── page.tsx                     # MODIFY: conditional redirect (auth → /home, else → /login)
│   │   ├── (authenticated)/
│   │   │   ├── layout.tsx               # NEW: sidebar layout wrapper
│   │   │   ├── home/
│   │   │   │   └── page.tsx             # NEW: home page dashboard
│   │   │   ├── tasks/                   # MOVE: existing tasks pages
│   │   │   ├── processes/               # MOVE: existing process pages
│   │   │   ├── forms/                   # MOVE: existing forms pages
│   │   │   ├── dashboard/               # MOVE: existing dashboard pages
│   │   │   └── admin/                   # MOVE: existing admin pages
│   │   └── login/
│   │       └── page.tsx                 # MODIFY: redirect to /home after login
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Sidebar.tsx              # NEW: collapsible sidebar navigation
│   │   │   ├── MobileNav.tsx            # NEW: hamburger menu for mobile
│   │   │   └── UserProfileMenu.tsx      # NEW: user dropdown with logout
│   │   └── home/
│   │       ├── SummaryCard.tsx           # NEW: stat card (task count, process count)
│   │       ├── ActivityFeed.tsx          # NEW: recent activity list
│   │       └── QuickActionCard.tsx       # NEW: navigation shortcut card
│   ├── contexts/
│   │   └── AuthContext.tsx              # NEW: auth state provider
│   └── lib/
│       ├── auth.ts                      # MODIFY: store/retrieve displayName, roles
│       └── api.ts                       # NO CHANGE
└── src/test/
    └── components/
        ├── Sidebar.test.tsx             # NEW
        └── HomePage.test.tsx            # NEW
```

**Structure Decision**: Web application structure (Option 2). Frontend changes are the primary scope — new route group `(authenticated)` with shared sidebar layout. Backend adds one new controller and enhances existing auth endpoints. No database migrations needed.

## Complexity Tracking

No constitution violations. The feature uses existing patterns (API endpoints, Client Components, Flowable services) with no new abstractions or workarounds.
