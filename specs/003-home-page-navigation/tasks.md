# Tasks: Home Page & Navigation

**Input**: Design documents from `/specs/003-home-page-navigation/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Auth context, route restructuring, and backend DTOs needed by all user stories

- [X] T001 Create AuthContext provider with user info (userId, username, displayName, roles, tenantCode) and auth state in `frontend/src/contexts/AuthContext.tsx`
- [X] T002 Update `frontend/src/lib/auth.ts` to store and retrieve displayName and roles from localStorage alongside token/tenantId
- [X] T003 [P] Create HomeSummaryDTO in `backend/src/main/java/com/flowable/platform/dto/HomeSummaryDTO.java`
- [X] T004 [P] Create ActivityItemDTO in `backend/src/main/java/com/flowable/platform/dto/ActivityItemDTO.java`
- [X] T005 [P] Create UserInfoDTO in `backend/src/main/java/com/flowable/platform/dto/UserInfoDTO.java`
- [X] T006 Add displayName and roles fields to LoginResponse DTO in `backend/src/main/java/com/flowable/platform/dto/LoginResponse.java`
- [X] T007 Update AuthController login method to populate displayName and roles from User entity in `backend/src/main/java/com/flowable/platform/controller/AuthController.java`
- [X] T008 Implement full `/api/auth/me` endpoint in AuthController to return UserInfoDTO with roles from SecurityContext in `backend/src/main/java/com/flowable/platform/controller/AuthController.java`
- [X] T009 Restructure frontend routes — create `frontend/src/app/(authenticated)/layout.tsx` route group and move existing pages (tasks/, processes/, forms/, dashboard/, admin/) under `frontend/src/app/(authenticated)/`. Login page MUST remain at `frontend/src/app/login/` outside the route group (FR-013: no nav on login)
- [X] T010 Update `frontend/src/app/page.tsx` root route to check auth state and redirect to `/home` (authenticated) or `/login` (not authenticated)
- [X] T011 Update `frontend/src/app/login/page.tsx` to store displayName/roles from login response and redirect to `/home` instead of `/tasks`

**Checkpoint**: Auth infrastructure ready, route structure reorganized, backend DTOs created

---

## Phase 2: User Story 1 - Home Page Dashboard (Priority: P1) 🎯 MVP

**Goal**: After login, users land on a home page showing pending task count, active process count, recent activity feed, and quick-action navigation cards

**Independent Test**: Log in → verify redirect to `/home` → verify summary widgets display data → verify quick-action cards link to correct sections

### Implementation for User Story 1

- [X] T012 [US1] Create HomeSummaryService in `backend/src/main/java/com/flowable/platform/service/HomeSummaryService.java` — query TaskService for pending task count, RuntimeService for active process count, HistoryService for last 5 activity items
- [X] T013 [US1] Create HomeController with GET /api/home/summary endpoint in `backend/src/main/java/com/flowable/platform/controller/HomeController.java`
- [X] T014 [P] [US1] Create SummaryCard component (icon, count, label, link) in `frontend/src/components/home/SummaryCard.tsx`
- [X] T015 [P] [US1] Create ActivityFeed component (list of 5 activity items with type icon, title, timestamp, "View all" link) in `frontend/src/components/home/ActivityFeed.tsx`
- [X] T016 [P] [US1] Create QuickActionCard component (icon, label, route link) in `frontend/src/components/home/QuickActionCard.tsx`
- [X] T017 [US1] Create home page at `frontend/src/app/(authenticated)/home/page.tsx` — compose SummaryCards (pending tasks, active processes), ActivityFeed, QuickActionCards (Tasks, Processes, Forms, Dashboard) with skeleton loading states and empty-state messages
- [X] T018 [US1] Add API call from home page to `GET /api/home/summary` using existing apiClient in `frontend/src/lib/api.ts`

**Checkpoint**: Home page dashboard fully functional — shows real data, skeleton loading, empty states, quick-action navigation

---

## Phase 3: User Story 2 - Global Navigation (Priority: P1)

**Goal**: A persistent left sidebar on all authenticated pages with collapsible behavior, active section highlighting, role-based Admin visibility, and mobile-responsive hamburger menu

**Independent Test**: Navigate between pages → verify sidebar persists → verify active highlight changes → collapse sidebar → verify icon-only mode → resize to mobile → verify hamburger menu

### Implementation for User Story 2

- [X] T019 [US2] Define navigation items configuration (label, path, icon, requiredRole) as a constant in `frontend/src/components/layout/navigation-items.ts`
- [X] T020 [US2] Create Sidebar component in `frontend/src/components/layout/Sidebar.tsx` — collapsible left sidebar with Lucide icons, labels, active route highlighting via `usePathname()`, role-based filtering (hide Admin for non-ADMIN), collapse toggle persisted to localStorage
- [X] T021 [US2] Create MobileNav component in `frontend/src/components/layout/MobileNav.tsx` — hamburger button in top bar, slide-out drawer overlay, click-outside-to-close, auto-close on navigation
- [X] T022 [US2] Implement the authenticated layout in `frontend/src/app/(authenticated)/layout.tsx` — wrap children with AuthContext provider, render Sidebar (desktop ≥768px) or MobileNav (mobile <768px), main content area with proper spacing

**Checkpoint**: Sidebar navigation works on all authenticated pages, collapses, highlights active section, hides Admin for non-ADMIN, mobile hamburger menu works

---

## Phase 4: User Story 3 - User Profile Menu (Priority: P2)

**Goal**: Display logged-in user's name in the sidebar with a dropdown menu providing logout functionality

**Independent Test**: Verify user's display name appears in sidebar → click dropdown → verify logout redirects to login and clears session

### Implementation for User Story 3

- [X] T023 [US3] Create UserProfileMenu component in `frontend/src/components/layout/UserProfileMenu.tsx` — shows avatar placeholder + display name (from AuthContext), dropdown with "Log out" option, calls authService.logout() and redirects to /login
- [X] T024 [US3] Integrate UserProfileMenu into Sidebar component at bottom of sidebar in `frontend/src/components/layout/Sidebar.tsx`
- [X] T025 [US3] Ensure UserProfileMenu works in collapsed sidebar (show avatar only) and mobile nav in `frontend/src/components/layout/MobileNav.tsx`

**Checkpoint**: User profile visible in sidebar, logout works, session cleared properly

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Accessibility, edge cases, and final validation

- [X] T026 [P] Add keyboard navigation (Tab/Enter/Escape) to sidebar menu items and profile dropdown in `frontend/src/components/layout/Sidebar.tsx`
- [X] T027 [P] Add ARIA roles and labels — `nav` landmark for sidebar, `aria-current="page"` for active item, `aria-expanded` for collapse state in `frontend/src/components/layout/Sidebar.tsx`
- [X] T028 [P] Handle unauthorized route access — redirect non-ADMIN users to `/home` when accessing `/admin/*` routes in `frontend/src/app/(authenticated)/admin/layout.tsx`
- [X] T029 [P] Add error boundary for home page summary data failures — show "Unable to load" per section instead of full page crash in `frontend/src/app/(authenticated)/home/page.tsx`
- [X] T030 Run quickstart.md validation — verify all 9 verification steps pass end-to-end. Additionally verify: login page has NO sidebar (FR-013), 375px viewport is usable (SC-006)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **US1 - Home Page (Phase 2)**: Depends on T001-T011 (Setup) — needs auth context, route structure, backend DTOs
- **US2 - Navigation (Phase 3)**: Depends on T001-T011 (Setup) — needs auth context and route group
- **US3 - Profile Menu (Phase 4)**: Depends on T020 (Sidebar component from US2)
- **Polish (Phase 5)**: Depends on all user story phases

### User Story Dependencies

- **US1 (P1)**: Independent after Setup — home page can work without sidebar
- **US2 (P1)**: Independent after Setup — sidebar can work without home page content
- **US3 (P2)**: Depends on US2 (needs sidebar component to integrate into)
- **US1 and US2 can run in parallel** after Setup completes

### Within Each User Story

- Backend DTOs/services before frontend components
- Reusable components before page composition
- Core functionality before edge cases

### Parallel Opportunities

- T003, T004, T005 (backend DTOs) can all run in parallel
- T014, T015, T016 (home page components) can all run in parallel
- US1 and US2 can be worked on in parallel after Setup
- T026, T027, T028, T029 (polish tasks) can all run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all home page components together (T014, T015, T016):
Task: "Create SummaryCard component in frontend/src/components/home/SummaryCard.tsx"
Task: "Create ActivityFeed component in frontend/src/components/home/ActivityFeed.tsx"
Task: "Create QuickActionCard component in frontend/src/components/home/QuickActionCard.tsx"
```

## Parallel Example: Setup DTOs

```bash
# Launch all backend DTOs together (T003, T004, T005):
Task: "Create HomeSummaryDTO in backend/src/main/java/com/flowable/platform/dto/HomeSummaryDTO.java"
Task: "Create ActivityItemDTO in backend/src/main/java/com/flowable/platform/dto/ActivityItemDTO.java"
Task: "Create UserInfoDTO in backend/src/main/java/com/flowable/platform/dto/UserInfoDTO.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 + User Story 2)

1. Complete Phase 1: Setup (auth context, route restructuring, DTOs)
2. Complete Phase 2: US1 - Home Page Dashboard
3. Complete Phase 3: US2 - Global Navigation
4. **STOP and VALIDATE**: Home page works, sidebar works, navigation works
5. Deploy/demo with core experience

### Incremental Delivery

1. Setup → Foundation ready
2. US1 (Home Page) → Users land on dashboard after login (MVP core)
3. US2 (Navigation) → Sidebar on all pages, mobile-responsive (MVP complete)
4. US3 (Profile Menu) → User identity + logout in sidebar
5. Polish → Accessibility, edge cases, validation

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- No database migrations needed — all data comes from existing Flowable engine and user tables
- Route restructuring (T009) is the highest-risk task — moving existing pages under `(authenticated)/` group
- Existing pages should continue working at same URLs after route group move
- Frontend icons use `lucide-react` (already in package.json)
