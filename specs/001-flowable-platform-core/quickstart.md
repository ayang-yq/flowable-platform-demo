# Quickstart Guide: Flowable Platform

**Version**: 1.0.0
**Purpose**: Get Flowable Platform up and running in 5 minutes
**Audience**: Developers, DevOps engineers

---

## Prerequisites

**Required Software**:
- Docker 20.10+ : [Download](https://docs.docker.com/get-docker/)
- Docker Compose 2.0+ : [Download](https://docs.docker.com/compose/install/)
- Git : [Download](https://git-scm.com/downloads)
- Modern browser (Chrome, Firefox, Safari, Edge) : Latest version

**System Requirements**:
- 8GB RAM minimum (16GB recommended)
- 20GB free disk space
- 4 CPU cores minimum (8 recommended)
- Ports 3000, 8080, 5432 available

**Optional** (for development):
- Java 21+: [Download](https://adoptium.net/)
- Node.js 20+: [Download](https://nodejs.org/)
- Maven 3.9+: [Download](https://maven.apache.org/)

---

## 5-Minute Startup

### 1. Clone Repository

```bash
git clone https://github.com/your-org/flowable-platform-demo.git
cd flowable-platform-demo
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env file with your settings
nano .env
```

**Required Environment Variables**:
```env
# Database Configuration
POSTGRES_PASSWORD=your_secure_password_here

# Backend Configuration
SPRING_PROFILES_ACTIVE=docker
JAVA_OPTS=-Xms512m -Xmx1024m

# Frontend Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080
NODE_ENV=production

# OAuth2 (Optional - for Azure AD)
AZURE_CLIENT_ID=your-azure-client-id
AZURE_CLIENT_SECRET=your-azure-client-secret
AZURE_TENANT_ID=your-azure-tenant-id
```

### 3. Start Services

```bash
# Build and start all services (PostgreSQL, Backend, Frontend)
docker-compose up -d
```

**Expected Output**:
```
[+] Running 3/3
✔ Network flowable-network         Created
✔ Container flowable-postgres      Started
✔ Container flowable-backend       Started
✔ Container flowable-frontend      Started
```

### 4. Wait for Services to be Healthy

```bash
# Check service health
docker-compose ps

# All containers should show "Up" status
# Wait approximately 2 minutes for all services to be ready
```

**Health Check Commands**:
```bash
# Backend health check
curl http://localhost:8080/actuator/health

# Expected response: {"status":"UP"}

# Frontend access
curl http://localhost:3000

# Expected: HTML response (login page)
```

### 5. Access Application

Open browser and navigate to:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Database**: localhost:5432

**Default Login**:
```yaml
Username: admin
Password: admin123
Tenant: acme (default tenant)
```

---

## Verification Checklist

- [ ] **Frontend Loads**: Browser displays login page at http://localhost:3000
- [ ] **Backend Healthy**: curl http://localhost:8080/actuator/health returns `{"status":"UP"}`
- [ ] **Database Ready**: Can connect to PostgreSQL at localhost:5432
- [ ] **Login Successful**: Can login with default credentials
- [ ] **Create First Process**: Can deploy a sample BPMN process
- [ ] **Start Process Instance**: Can start a process instance
- [ ] **Complete Task**: Can view and complete first task

---

## Development Setup

### Prerequisites

**Backend Development**:
```bash
# Java 21
java -version

# Maven 3.9+
mvn -version

# PostgreSQL client
psql --version
```

**Frontend Development**:
```bash
# Node.js 20+
node --version
npm --version

# Or use nvm
nvm install 20
nvm use 20
```

### Start Backend (Development Mode)

```bash
cd backend

# Start backend with Spring Boot dev tools
mvn spring-boot:run

# Backend runs on http://localhost:8080
# Hot reload enabled for Java classes
```

### Start Frontend (Development Mode)

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Frontend runs on http://localhost:3000
# Hot module reload (HMR) enabled
```

### Start Database (Standalone)

```bash
# Only database container
docker-compose up postgres

# Or start PostgreSQL locally (Mac/Linux)
brew services start postgresql
# Or use your local PostgreSQL installation
```

---

## Docker Compose Commands

### Service Management

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose stop

# Restart services
docker-compose restart

# Stop and remove containers (preserve data volumes)
docker-compose down

# Stop and remove everything including data (⚠️ DELETES ALL DATA)
docker-compose down -v

# View logs (all services)
docker-compose logs -f

# View logs (specific service)
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Rebuild images (after code changes)
docker-compose build --no-cache
docker-compose up -d
```

### Container Management

```bash
# Enter container shell (debugging)
docker-compose exec backend sh
docker-compose exec frontend sh
docker-compose exec postgres psql -U flowable -d flowable_platform

# View container resource usage
docker stats

# View container details
docker-compose ps
```

---

## First Process Walkthrough

### 1. Prepare Sample BPMN Process

Create a simple approval process: `samples/leave-request.bpmn20.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/2.0"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/2.0/DI"
             typeLanguage="http://www.w3.org/2001/XMLSchema"
             targetNamespace="http://bpmn.io/schema/bpmn">
  <process id="leaveRequest" name="Leave Request Process" isExecutable="true">
    <startEvent id="start"/>
    <sequenceFlow sourceRef="start" targetRef="submitLeave"/>
    <userTask id="submitLeave" name="Submit Leave Request"/>
    <sequenceFlow sourceRef="submitLeave" targetRef="managerApproval"/>
    <userTask id="managerApproval" name="Manager Approval"/>
    <sequenceFlow sourceRef="managerApproval" targetRef="end"/>
    <endEvent id="end"/>
  </process>
</definitions>
```

### 2. Deploy Process Definition

**Option A**: Through Admin Console
1. Login as admin
2. Navigate to Admin → Process Definitions
3. Click "Upload Process Definition"
4. Select `leave-request.bpmn20.xml`
5. Click "Deploy"

**Option B**: Through API
```bash
curl -X POST http://localhost:8080/api/processes/definitions \
  -H "X-Tenant-Id: {tenant-uuid}" \
  -H "Authorization: Bearer {jwt-token}" \
  -F "file=@samples/leave-request.bpmn20.xml"
```

### 3. Create Form Schema

**Option A**: Through Visual Form Builder
1. Navigate to Forms → Create Form
2. Drag and drop form fields (startDate, endDate, reason, days)
3. Configure validation and permissions
4. Save as "Leave Request Form v1.0.0"
5. Associate with `leaveRequest` process and `managerApproval` task

**Option B**: Import JSON Schema
```json
{
  "name": "Leave Request Form",
  "version": "1.0.0",
  "processDefinitionKey": "leaveRequest",
  "taskDefinitionKey": "managerApproval",
  "fields": [
    {
      "id": "startDate",
      "type": "date",
      "label": "Start Date",
      "required": true
    },
    {
      "id": "endDate",
      "type": "date",
      "label": "End Date",
      "required": true
    },
    {
      "id": "reason",
      "type": "textarea",
      "label": "Reason",
      "required": true
    },
    {
      "id": "days",
      "type": "number",
      "label": "Number of Days",
      "required": true
    }
  ]
}
```

### 4. Start Process Instance

```bash
curl -X POST http://localhost:8080/api/processes \
  -H "X-Tenant-Id: {tenant-uuid}" \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "processDefinitionKey": "leaveRequest",
    "businessKey": "LR-2024-001",
    "variables": {
      "employeeName": "John Doe",
      "startDate": "2026-04-01",
      "endDate": "2026-04-05",
      "reason": "Family vacation",
      "days": 5
    }
  }'
```

### 5. Complete Task

1. Navigate to Tasks → My Tasks
2. Click on "Manager Approval" task
3. Fill in form (optional if all fields required)
4. Click "Complete" button
5. Add approval comment (required)
6. Submit form

### 6. Verify Process Completion

1. Navigate to Tasks → My Requests
2. Click on the process instance you started
3. Verify status shows "COMPLETED"
4. View process diagram showing completed path

---

## Troubleshooting

### Issue: Port Already in Use

**Error**: `Bind for 0.0.0.0:3000 failed: port is already allocated`

**Solution**:
```bash
# Check what's using the port
lsof -i :3000  # macOS
netstat -ano | findstr :3000  # Windows

# Stop conflicting service or change port in docker-compose.yml
```

### Issue: Database Connection Errors

**Error**: `Connection refused: localhost:5432`

**Solution**:
```bash
# Check PostgreSQL container status
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Verify database is ready
docker-compose exec postgres pg_isready -U flowable
```

### Issue: Backend Cannot Connect to Database

**Error**: `HikariPool-1 - Exception during pool initialization`

**Solution**:
```bash
# Verify database is healthy first
docker-compose exec postgres psql -U flowable -d flowable_platform

# Check environment variables in .env
cat .env | grep SPRING_DATASOURCE

# Restart backend after database is ready
docker-compose restart backend
```

### Issue: Frontend Cannot Connect to Backend API

**Error**: `Network error when attempting to fetch resource`

**Solution**:
```bash
# Check backend is healthy
curl http://localhost:8080/actuator/health

# Check NEXT_PUBLIC_API_URL in .env
cat .env | grep NEXT_PUBLIC_API_URL

# Verify backend is accessible from frontend container
docker-compose exec frontend ping backend

# Check frontend logs for CORS errors
docker-compose logs frontend
```

### Issue: Containers Keep Restarting

**Error**: Container status shows `Restarting (1) 2 seconds ago`

**Solution**:
```bash
# Check container logs for errors
docker-compose logs backend
docker-compose logs frontend

# Common causes:
# - Out of memory (increase JAVA_OPTS in .env)
# - Application crash (check stack trace in logs)
# - Configuration error (verify .env settings)

# Restart with verbose logging
docker-compose down
docker-compose up
```

### Issue: Process Instance Gets Stuck

**Symptom**: Task not advancing, no errors visible

**Solution**:
```bash
# Check Flowable job executor logs
docker-compose logs backend | grep "job"

# Use admin console to investigate
# Navigate to Admin → Process Instances
# Select stuck instance
# Click "View Variables" to check process data
# Use "Modify Variables" to fix data errors
# Use "Jump to Node" to skip problematic node
```

---

## Next Steps

### Learn More

- **Documentation**: Read [architecture.md](../../architecture.md) for system design
- **Constitution**: Review [.specify/memory/constitution.md](../../.specify/memory/constitution.md) for technical principles
- **API Reference**: See [contracts/api-endpoints.md](contracts/api-endpoints.md) for complete API documentation
- **Form Builder**: Explore form schema contracts in [contracts/form-schema.md](contracts/form-schema.md)

### Development Workflow

1. **Implement Features**: Follow user stories in [spec.md](spec.md) (P1 → P2 → P3 priority)
2. **Test Locally**: Run backend integration tests and frontend component tests
3. **Deploy Changes**: Rebuild Docker images and restart containers
4. **Monitor Health**: Check `/actuator/health` and Prometheus metrics

### Production Deployment

1. **Security**:
   - Change default passwords
   - Configure HTTPS (SSL certificates)
   - Use strong JWT secret keys
   - Enable firewall rules

2. **Scaling**:
   - Use Kubernetes or Docker Swarm for orchestration
   - Enable horizontal pod autoscaling
   - Configure load balancer (HAProxy, Nginx)

3. **Monitoring**:
   - Set up Prometheus + Grafana monitoring
   - Configure log aggregation (ELK stack)
   - Enable alerting rules

---

## Getting Help

### Resources

- **Documentation**: [README.md](../../README.md)
- **Architecture**: [architecture.md](../../architecture.md)
- **Issues**: [GitHub Issues](https://github.com/your-org/flowable-platform-demo/issues)
- **Support**: support@acme.com

### Common Commands Reference

```bash
# Service lifecycle
docker-compose up -d          # Start services
docker-compose stop           # Stop services
docker-compose down            # Stop and remove
docker-compose down -v         # Delete everything (including data!)

# Logs and debugging
docker-compose logs -f          # Follow all logs
docker-compose logs backend     # Backend logs only
docker-compose exec backend sh  # Shell access

# Database
docker-compose exec postgres psql -U flowable -d flowable_platform

# Rebuild after code changes
docker-compose build --no-cache backend
docker-compose build --no-cache frontend
docker-compose up -d

# Reset everything (⚠️ DELETES DATA)
docker-compose down -v
docker-compose up -d --force-recreate
```

---

**Platform is now running! Start building your workflows.** 🚀
