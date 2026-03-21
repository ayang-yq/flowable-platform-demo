# Flowable Platform Demo

Multi-tenant workflow platform built with Flowable 7.x, Spring Boot 3.5.x, and Next.js 14+.

## Quick Start

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- Git

### 5-Minute Startup

1. **Clone repository**
   ```bash
   git clone https://github.com/your-org/flowable-platform-demo.git
   cd flowable-platform-demo
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   nano .env  # Edit with your settings
   ```

3. **Start services**
   ```bash
   docker-compose up -d
   ```

4. **Wait for services** (~2 minutes)
   ```bash
   docker-compose ps
   ```

5. **Access application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - Database: localhost:5433 (mapped from container port 5432 to avoid conflicts with local PostgreSQL)

6. **Default login**
   ```
   Username: admin
   Password: admin123
   Tenant: acme
   ```

## Documentation

- **Quickstart Guide**: [specs/001-flowable-platform-core/quickstart.md](specs/001-flowable-platform-core/quickstart.md)
- **API Reference**: [specs/001-flowable-platform-core/contracts/api-endpoints.md](specs/001-flowable-platform-core/contracts/api-endpoints.md)
- **Data Model**: [specs/001-flowable-platform-core/data-model.md](specs/001-flowable-platform-core/data-model.md)
- **Implementation Plan**: [specs/001-flowable-platform-core/plan.md](specs/001-flowable-platform-core/plan.md)
- **Tasks**: [specs/001-flowable-platform-core/tasks.md](specs/001-flowable-platform-core/tasks.md)

## Development

### Backend Development
```bash
cd backend
mvn spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm run dev
```

### Database Only
```bash
docker-compose up postgres
```

## Features

- ✅ **BPMN 2.0 / CMMN 1.1 / DMN 1.3** workflow engine
- ✅ **Multi-tenant** data isolation
- ✅ **OAuth2/OIDC** authentication (Azure AD support)
- ✅ **Dynamic forms** with visual builder (SurveyJS)
- ✅ **Task center** with assignment, delegation, CC
- ✅ **Collaboration** (comments, attachments, @mentions)
- ✅ **Admin console** for process management
- ✅ **Analytics dashboards** (ECharts)
- ✅ **Audit logging** with append-only enforcement

## Architecture

- **Backend**: Spring Boot 3.5.x + Flowable 7.x + PostgreSQL 15+
- **Frontend**: Next.js 14+ (App Router) + React 18+ + Tailwind CSS + Shadcn/UI
- **Testing**: JUnit 5, Testcontainers, Jest, React Testing Library

## License

MIT License - see LICENSE file for details

## Support

For issues and questions, please open a GitHub issue.
