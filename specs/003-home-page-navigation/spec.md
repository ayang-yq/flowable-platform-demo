# Feature Specification: Home Page & Navigation

**Feature Branch**: `003-home-page-navigation`
**Created**: 2026-03-22
**Status**: Draft
**Input**: User description: "Create a home page, redirect to home page after login. i can go to other pages from home page"

## Clarifications

### Session 2026-03-22

- Q: Navigation layout style (sidebar vs top-bar)? → A: Left sidebar (vertical, collapsible, with icons and labels)
- Q: Loading and empty state behavior? → A: Skeleton placeholders while loading; friendly empty-state messages with call-to-action links when zero items
- Q: How many items in the recent activity feed? → A: Fixed at 5 items with a "View all" link
- Q: At what breakpoint does the sidebar collapse to mobile hamburger menu? → A: 768px (Tailwind md breakpoint)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Home Page Dashboard (Priority: P1)

After logging in, users land on a home page that provides a quick overview of their work and easy access to all platform sections. The home page shows a summary of pending tasks, recent processes, and quick-action shortcuts so users can immediately understand their workload and navigate to what matters most.

**Why this priority**: The home page is the central hub of the application. Without it, users land on a specific page (tasks) with no way to navigate to other sections. This is the foundational experience.

**Independent Test**: Can be fully tested by logging in and verifying the home page loads with summary widgets and navigation links to all major sections.

**Acceptance Scenarios**:

1. **Given** a user has just logged in, **When** authentication succeeds, **Then** the user is redirected to the home page (`/home`)
2. **Given** a user is on the home page, **When** the page loads, **Then** they see a summary of their pending tasks count, active processes count, and recent activity
3. **Given** an unauthenticated user navigates to `/home`, **When** the page loads, **Then** they are redirected to the login page
4. **Given** a user visits the root URL (`/`), **When** they are authenticated, **Then** they are redirected to `/home` instead of `/login`

---

### User Story 2 - Global Navigation (Priority: P1)

A persistent left sidebar is visible on all authenticated pages, allowing users to move between sections (Home, Tasks, Processes, Forms, Dashboard, Admin) without returning to the home page first. The sidebar displays icons and labels, is collapsible, and highlights the currently active section.

**Why this priority**: Navigation is equally critical to the home page — without it, users are trapped on whichever page they land on. This must ship together with the home page.

**Independent Test**: Can be tested by navigating between any two pages and verifying the navigation bar persists and highlights the correct section.

**Acceptance Scenarios**:

1. **Given** a user is on any authenticated page, **When** the page renders, **Then** a navigation bar is visible with links to: Home, Tasks, Processes, Forms, Dashboard, and Admin
2. **Given** a user is on the Tasks page, **When** they look at the navigation, **Then** the "Tasks" link is visually highlighted as the active section
3. **Given** a user clicks "Processes" in the navigation, **When** the page transitions, **Then** the Processes page loads and "Processes" becomes the highlighted link
4. **Given** a user is on the login page, **When** the page renders, **Then** the navigation bar is NOT shown

---

### User Story 3 - User Profile Menu (Priority: P2)

The navigation includes a user profile area showing the logged-in user's name and a dropdown menu with a log out option. This provides identity context and a clear way to end the session.

**Why this priority**: Users need to know who they're logged in as and have a way to log out. This is important but secondary to the core home page and navigation.

**Independent Test**: Can be tested by verifying the user's name appears in the navigation and the logout action ends the session and returns to the login page.

**Acceptance Scenarios**:

1. **Given** a user is logged in, **When** they look at the navigation bar, **Then** they see their display name or username
2. **Given** a user clicks the logout option, **When** the logout completes, **Then** they are redirected to the login page and their session is cleared
3. **Given** a user has logged out, **When** they try to navigate to `/home`, **Then** they are redirected to the login page

---

### Edge Cases

- What happens when a user bookmarks `/home` and their session has expired? They should be redirected to login.
- What happens when the summary data (task count, process count) fails to load? The home page should still render with graceful error states showing "Unable to load" instead of crashing.
- What happens on mobile screen sizes (below 768px)? The sidebar collapses into a hamburger-triggered overlay or slide-out drawer.
- What happens if a user does not have the ADMIN role? The "Admin" navigation link should be hidden.
- What does the home page show while summary data is loading? Skeleton placeholders are displayed for each summary panel and the activity feed until data arrives.
- What does the home page show when a user has zero pending tasks or zero active processes? A friendly empty-state message with a call-to-action link (e.g., "No pending tasks — go to Tasks to get started").

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a home page at the `/home` route after successful authentication
- **FR-002**: System MUST redirect users from `/` to `/home` when authenticated, or to `/login` when not authenticated
- **FR-003**: System MUST redirect users to `/home` (instead of `/tasks`) after successful login
- **FR-004**: Home page MUST display a summary panel showing the user's pending task count
- **FR-005**: Home page MUST display a summary panel showing the user's active process instance count
- **FR-006**: Home page MUST display a recent activity feed showing the last 5 actions (task completions, process starts, task assignments) with a "View all" link to the full activity history
- **FR-007**: Home page MUST provide quick-action buttons or cards to navigate to key sections (Tasks, Processes, Forms, Dashboard)
- **FR-008**: System MUST display a persistent left sidebar with icons and labels on all authenticated pages, collapsible to icon-only mode
- **FR-009**: Navigation MUST include links to: Home, Tasks, Processes, Forms, Dashboard, and Admin
- **FR-010**: Navigation MUST visually indicate which section is currently active
- **FR-011**: Navigation MUST show the logged-in user's name and provide a logout action
- **FR-012**: Navigation MUST hide the Admin link for users without the ADMIN role
- **FR-013**: Navigation MUST NOT appear on the login page
- **FR-014**: Navigation MUST be responsive — below 768px (Tailwind md breakpoint), the sidebar collapses into a hamburger-triggered slide-out drawer
- **FR-015**: System MUST redirect unauthenticated users to the login page when they attempt to access any authenticated route
- **FR-016**: Home page MUST display skeleton placeholders for summary panels and activity feed while data is loading
- **FR-017**: Home page MUST display friendly empty-state messages with call-to-action links when a user has zero pending tasks, zero active processes, or no recent activity

### Key Entities

- **Home Summary**: Aggregated view combining pending task count, active process count, and recent activity items — not a persisted entity, but a computed view from existing data
- **Navigation Item**: A menu entry with label, route path, icon, and optional role-based visibility rules

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users land on the home page within 1 second of successful login
- **SC-002**: Users can navigate from any page to any other section in a single click via the navigation bar
- **SC-003**: The home page loads with summary data within 2 seconds on a standard connection
- **SC-004**: 100% of authenticated pages display the navigation bar
- **SC-005**: Users can identify their current location in the app by looking at the navigation highlighting
- **SC-006**: The navigation is usable on screens as small as 375px wide (mobile)

## Assumptions

- The existing authentication system (JWT-based) and auth service are used as-is; no changes to login mechanics beyond the redirect target
- Summary data (task counts, process counts) is fetched from existing API endpoints (`/api/tasks`, `/api/processes`)
- The "recent activity" feed will use existing audit log data or task/process history
- The Admin section is role-gated based on the user's roles from the JWT token
- The navigation uses a left sidebar layout consistent with the existing Tailwind CSS styling
