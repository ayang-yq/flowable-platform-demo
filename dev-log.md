# Development Log - Flowable Platform Demo
## Audit trail of all development activities

---

## [2026-03-21] - PostgreSQL Data Persistence Question

**Timestamp**: 2026-03-21 13:45:00 UTC
**User Request**:
> /btw, why i restart postgres container the data still there

**Actions Performed**:
- ✅ Logged request to dev-log.md (MANDATORY FIRST STEP)
- ✅ Read docker-compose.yml to check PostgreSQL volume configuration
- ✅ Identified volume mount configuration for PostgreSQL data persistence
- ✅ Analyzed Docker volume storage mechanism

**Files Read**:
- `docker-compose.yml`

**Outcome**: Explained Docker volume persistence mechanism for PostgreSQL data.

---

## [2026-03-21] - Fix Login Authentication Issue

**Timestamp**: 2026-03-21 13:30:00 UTC
**User Request**:
> got invalid credentials for curl -X 'POST' 'http://localhost:8080/api/auth/login' -H 'accept: */*' -H 'Content-Type: application/json' -d '{"username": "admin", "password": "admin123", "tenantCode": "acme"}'

**Actions Performed**:
- Used systematic debugging skill to trace root cause
- Checked service logs, database state, and authentication flow
- Discovered port 8080 was occupied by old service instance
- Identified BCrypt password hash mismatch issue
- Generated correct BCrypt hashes using Spring Security BCryptPasswordEncoder
- Updated database with working password hash: `$2a$10$n7AyxDyB/0PIkeWoMMg16ezCYx5PsnEoCEpP3dIIrchD7DGjCMIUq`
- Updated seed data SQL file with correct password hashes
- Cleaned up debug logging from AuthController

**Files Modified**:
- `backend/src/main/java/com/flowable/platform/controller/AuthController.java`
- `backend/src/main/resources/db/migration/V2__insert_seed_data.sql`

**Outcome**: ✅ Login authentication now working correctly. Admin user can successfully authenticate with password `admin123` and receive JWT tokens.

**Commit**: `1137f83` - fix: correct BCrypt password hashes in seed data for login

---

## [2026-03-21] - Fix Swagger 500 Error and Unit Test Failures

**Timestamp**: 2026-03-21 12:00:00 UTC
**User Request**:
> i have started the backend service, check why swagger not working, i got 500 error /v3/api-docs/platform
> unit test failed after the fix

**Actions Performed**:
- Investigated Spring Boot 3.5.0 compatibility issues
- Upgraded springdoc-openapi-starter-webmvc-ui from 2.6.0 to 2.8.15
- Added `spring.mvc.pathmatch.matching-strategy: ant_path_matcher` for URL pattern compatibility
- Disabled SpringDoc autoconfiguration in tests to avoid pattern validation errors
- Fixed AuditLog entity JSONB mapping from `columnDefinition = "JSONB"` to `@JdbcTypeCode(SqlTypes.JSON)` for Hibernate 6+ compatibility
- Upgraded Spring Boot from 3.5.0 to 3.5.12 and Flowable from 7.0.0 to 7.2.0

**Files Modified**:
- `backend/pom.xml` (version upgrades)
- `backend/src/main/resources/application.yml` (path matching strategy)
- `backend/src/main/resources/application-test.yml` (disable SpringDoc in tests)
- `backend/src/main/java/com/flowable/platform/entity/AuditLog.java` (JSONB mapping fix)

**Outcome**: ✅ Swagger UI accessible at `/swagger-ui.html`, all unit tests passing, API documentation generating correctly.

**Commit**: `4ec8f14` - fix: resolve Spring Boot 3.5 compatibility issues with Swagger and tests

---

## [2026-03-21] - Fix Log File Append Issue

**Timestamp**: 2026-03-21 11:00:00 UTC
**User Request**:
> i have already set log append as false, but why the log file not being re-created when service start

**Actions Performed**:
- Investigated logback-spring.xml configuration
- Identified that `RollingFileAppender` with `TimeBasedRollingPolicy` was ignoring `append=false` parameter
- Replaced `RollingFileAppender` with simple `FileAppender` that respects `append=false`
- Removed time-based rolling policy (trade-off: lost daily log rotation for startup truncation)

**Files Modified**:
- `backend/src/main/resources/logback-spring.xml`

**Outcome**: ✅ Log file now recreated (truncated) on every service startup as expected.

**Commit**: `bd9d869` - fix: use FileAppender to recreate log file on every service startup

---

**Next Actions**:
- Monitor for log file size issues (no rotation means file grows until restart)
- Consider adding programmatic log rotation at startup if needed
- All user passwords currently set to `admin123` for testing - change to production credentials before deployment

---

**Note**: This file is maintained in reverse chronological order (newest entries at top)
