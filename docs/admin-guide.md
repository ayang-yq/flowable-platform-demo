# Administrator Guide

## User Management

### Managing Users (`/admin/users`)
- Create, edit, and deactivate user accounts
- Assign roles and departments
- Reset passwords

### Managing Departments (`/admin/departments`)
- Create hierarchical department structure
- Assign users to departments
- Departments support parent-child relationships with materialized paths

### Managing Roles (`/admin/roles`)
- Create custom roles with permission sets
- System roles (ADMIN, USER) cannot be modified or deleted
- Assign roles to users for access control

## Process Administration

### Process Definitions (`/admin/processes`)
- View all deployed process definitions
- Deploy new BPMN process files
- Manage process versions

### Instance Management (`/admin/instances`)
- View all running process instances across tenants
- **Suspend** - Pause a running instance
- **Activate** - Resume a suspended instance
- **Terminate** - Force-stop an instance

### Advanced Operations
- **Variable Modification** - Edit process variables on running instances
- **Node Jump** - Move execution from one activity to another (use with caution)

## Audit Logs (`/admin/audit`)
- Query audit logs by date range, user, action type
- View detailed audit log entries
- Export logs in CSV or JSON format
- Audit logs are append-only (cannot be modified or deleted)

## Data Dictionaries
- Manage dropdown/enumeration values used in forms
- Organize by category and code
- Control sort order and active status

## Monitoring

### Health Checks
- `GET /actuator/health` - Application health status
- `GET /actuator/prometheus` - Prometheus metrics endpoint

### Key Metrics
- `flowable.process.started` - Process instances started (counter)
- `flowable.task.completed` - Tasks completed (counter)
- `flowable.process.active` - Currently active instances (gauge)

### Grafana
Import the pre-built dashboard from `backend/src/main/resources/grafana-dashboards/flowable-platform.json` for:
- Process instance throughput
- Task completion rates
- JVM and database performance
- HTTP request latencies

## Scheduled Jobs
- **Audit Log Archival** - Runs daily at 2:00 AM, archives process instances older than 365 days
- Configure via `app.archival.cron` in application.yml

## Multi-Tenant Configuration
- Each tenant has isolated data via `tenant_id` column strategy
- Tenant context is set via `X-Tenant-Id` header or extracted from JWT token
- Users, departments, roles, and all business data are tenant-scoped
