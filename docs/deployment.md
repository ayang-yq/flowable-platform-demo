# Production Deployment Guide

## Prerequisites

- Java 21 (OpenJDK recommended)
- PostgreSQL 15+ with `uuid-ossp` extension
- Node.js 18+ (for frontend build)
- Docker 20.10+ (optional, for containerized deployment)

## Database Setup

1. Create database and enable extensions:
   ```sql
   CREATE DATABASE flowable_platform;
   \c flowable_platform
   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   ```

2. Flyway migrations run automatically on application startup.

## Backend Deployment

### Build
```bash
cd backend
mvn package -DskipTests -Pprod
```

### Run
```bash
java -jar target/flowable-platform-demo-*.jar \
  --spring.datasource.url=jdbc:postgresql://db-host:5432/flowable_platform \
  --spring.datasource.username=app_user \
  --spring.datasource.password=<secure-password> \
  --app.jwt.secret=<256-bit-secret> \
  --server.port=8080
```

### Environment Variables
| Variable | Description | Required |
|----------|-------------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Yes |
| `APP_JWT_SECRET` | JWT signing key (min 256 bits) | Yes |
| `AZURE_CLIENT_ID` | Azure AD client ID | For OAuth2 |
| `AZURE_CLIENT_SECRET` | Azure AD client secret | For OAuth2 |
| `AZURE_TENANT_ID` | Azure AD tenant ID | For OAuth2 |

## Frontend Deployment

### Build
```bash
cd frontend
npm ci
npm run build
```

### Deploy
Serve the `out/` or `.next/` directory with a reverse proxy (Nginx, Caddy, or cloud CDN).

## Docker Deployment

```bash
docker-compose -f docker-compose.yml up -d --build
```

## Monitoring

- **Health check**: `GET /actuator/health`
- **Prometheus metrics**: `GET /actuator/prometheus`
- **Grafana dashboards**: Import from `backend/src/main/resources/grafana-dashboards/`

## Backup Strategy

- Schedule `pg_dump` via cron for daily backups
- Enable WAL archiving for point-in-time recovery
- Test restore procedures regularly

## Security Checklist

- [ ] Change default admin password
- [ ] Configure HTTPS/TLS termination
- [ ] Restrict CORS origins to production domain
- [ ] Set strong JWT secret (256+ bits)
- [ ] Enable database connection SSL
- [ ] Configure firewall rules for database port
