# Quickstart: Home Page & Navigation

**Branch**: `003-home-page-navigation` | **Date**: 2026-03-22

## What This Feature Does

Adds a home page dashboard and persistent sidebar navigation to the Flowable Platform. After login, users land on `/home` with summary widgets (pending tasks, active processes, recent activity) and can navigate to any section via the sidebar.

## Key Integration Points

### 1. Login Redirect Change
**Before**: `router.push('/tasks')` in `frontend/src/app/login/page.tsx:22`
**After**: `router.push('/home')`

### 2. Root Route Change
**Before**: `redirect('/login')` in `frontend/src/app/page.tsx:4`
**After**: Redirect to `/home` if authenticated, `/login` if not

### 3. New Route Group: `(authenticated)`
All existing pages (`/tasks`, `/processes`, `/forms`, `/dashboard`, `/admin`) move under `frontend/src/app/(authenticated)/` to share the sidebar layout. The login page stays at `frontend/src/app/login/`.

### 4. New Backend Endpoint
`GET /api/home/summary` — returns aggregated dashboard data (task count, process count, recent activity).

### 5. Enhanced Auth Endpoints
- `POST /api/auth/login` — add `displayName` and `roles` to response
- `GET /api/auth/me` — return full user info with roles

## Quick Verification

1. Start backend: `cd backend && mvn spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Log in with `admin / admin123 / acme`
4. Verify redirect to `/home` (not `/tasks`)
5. Verify sidebar shows on left with Home, Tasks, Processes, Forms, Dashboard, Admin
6. Click sidebar items — page changes, active item highlights
7. Verify home page shows pending task count, active process count, recent activity
8. Collapse sidebar — only icons visible
9. Resize to mobile (<768px) — sidebar becomes hamburger menu
