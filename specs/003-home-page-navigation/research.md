# Research: Home Page & Navigation

**Branch**: `003-home-page-navigation` | **Date**: 2026-03-22

## Decision 1: Auth State Management (Client-Side)

**Decision**: Use a React Context provider wrapping authenticated routes that reads JWT from localStorage, decodes user info (username, roles), and exposes `user`, `isAuthenticated`, `logout` to all child components.

**Rationale**: The existing `authService` already stores JWT in localStorage. A context provider centralizes auth state so the sidebar, home page, and profile menu can all access user info without prop drilling. This aligns with Next.js App Router patterns where Client Components need shared state.

**Alternatives considered**:
- Server-side session check on every page load — adds latency, requires `/api/auth/me` endpoint to be fully implemented
- Zustand/Redux store — over-engineering for auth state that's already in localStorage

## Decision 2: Home Page Summary Data Fetching

**Decision**: Create a new backend endpoint `GET /api/home/summary` that aggregates pending task count, active process count, and recent activity (last 5 items) into a single response. The frontend home page calls this single endpoint.

**Rationale**: A single aggregated endpoint avoids 3 separate API calls from the client, reducing latency and simplifying loading/error states. The backend can efficiently query Flowable's TaskService and RuntimeService in parallel.

**Alternatives considered**:
- Three separate API calls (`/api/tasks/my-tasks?size=0`, `/api/processes?status=active`, `/api/audit?limit=5`) — more network requests, harder to coordinate loading states
- GraphQL — not in the tech stack, over-engineering for this use case

## Decision 3: Sidebar Layout Architecture

**Decision**: Create an `AuthenticatedLayout` component that wraps all authenticated routes. This layout includes the sidebar and renders `{children}` in the main content area. The login page uses its own layout without the sidebar.

**Rationale**: Next.js App Router supports nested layouts natively. An `(authenticated)` route group with its own `layout.tsx` provides the sidebar on all authenticated pages without conditional rendering. The login page sits outside this group.

**Alternatives considered**:
- Conditional sidebar in root layout — mixes concerns, requires auth check at root level
- Per-page sidebar import — duplicates code, inconsistent behavior

## Decision 4: Login Response Enhancement

**Decision**: Enhance the `/api/auth/login` response and `/api/auth/me` endpoint to include `displayName` and `roles` (list of role names). Store these in localStorage alongside the JWT token.

**Rationale**: The sidebar needs the user's display name and roles (to show/hide Admin link). The JWT token currently only contains `username` and `tenantId`. Rather than decoding the JWT client-side or adding roles to JWT claims, returning them in the login response is simpler and the `/api/auth/me` endpoint provides a refresh mechanism.

**Alternatives considered**:
- Add roles to JWT claims — increases token size, requires re-login for role changes
- Decode JWT client-side — requires jwt-decode dependency, and roles still aren't in the token

## Decision 5: Sidebar Collapse State Persistence

**Decision**: Persist sidebar collapsed state in localStorage so it survives page refreshes and navigation. Default to expanded on desktop, collapsed on mobile.

**Rationale**: Users expect their sidebar preference to persist. localStorage is the simplest persistence mechanism that doesn't require backend changes.

**Alternatives considered**:
- Cookie-based — works for SSR but adds complexity
- Backend user preferences — over-engineering for a UI preference

## Decision 6: Mobile Sidebar Behavior

**Decision**: Below 768px, the sidebar is hidden by default and opens as a slide-out overlay triggered by a hamburger button in a top bar. Clicking outside or navigating closes it.

**Rationale**: Standard mobile navigation pattern. Aligns with the spec's 768px breakpoint clarification and Tailwind's `md` breakpoint.

**Alternatives considered**:
- Bottom navigation bar on mobile — non-standard for admin/workflow apps
- Always-visible icon-only sidebar — takes too much horizontal space on mobile
