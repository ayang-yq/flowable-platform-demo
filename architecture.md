# 通用流程管理平台设计文档
**目标**：基于 Flowable 7.x 构建一个高性能、易扩展、多租户的通用流程管理与自动化平台。

## 一、 技术栈
### 1. 后端
* **核心框架**：Spring Boot 3.5.x + Spring Security (基于 Java 21)
* **流程引擎**：Flowable 7.x (BPMN, CMMN, DMN)
* **数据库**：PostgreSQL 
* **文档存储**：通过API集成第三方文档管理系统

### 2. 前端
* **核心框架**：Next.js (App Router, React 18+)
* **UI 与样式**：Tailwind CSS + Shadcn/UI (提供高度定制化的现代化组件)
* **表单引擎**：SurveyJS (通过 JSON Schema 实现动态表单渲染与数据收集)
* **数据可视化**：ECharts (用于流程监控与统计分析报表)

---

## 二、 功能模块

### 1. 核心流程引擎 (CMMN, BPMN and DMN)
* **高级流转控制**：加签、减签、会签（串行/并行）、驳回（退回到发起人或指定节点）、撤回（在下一节点审批前主动撤回）。
* **委托与代理**：支持任务临时委托（Delegate）与长期代理（Assignee 转移）。
* **规则与决策**：集成 DMN（决策需求图），实现复杂的业务规则自动判断（如：金额>1万走A流程，否则走B流程）。
* **流程可视化**：基于流转历史数据，在前端动态渲染流程图，并高亮当前节点与历史轨迹（可集成 `bpmn.js` 或 Flowable 官方前端组件）。
* **子流程与调用活动**：支持主子流程嵌套，处理复杂的大型业务流。

### 2. 任务中心 (Task Center)
* **待办与已办**：
    * 我的待办（My Tasks）：需要当前用户处理的任务。
    * 我的已办（Completed Tasks）：当前用户处理过的历史任务记录。
    * 我的发起（My Requests）：当前用户发起的流程实例跟踪。
* **所有任务列表**：管理员视角的全局任务监控面板。
* **任务协同**：
    * 过期任务提醒（SLA 管理）：支持配置任务超时时间，超时自动发送通知（邮件/站内信）或自动流转。
    * 支持临时任务（Ad-hoc Tasks）创建（结合 CMMN 实现非结构化的动态任务派发）。
    * 任务抄送（CC）：将任务信息知会给非审批人。

### 3. 表单渲染引擎 (Form Engine)
* **动态 JSON 表单**：利用 SurveyJS 基于 JSON Schema 定义表单结构，实现前后端表单结构解耦。
* **表单版本控制**：表单修改后生成新版本，历史流程实例依然使用旧版表单进行渲染，保证数据一致性。
* **表单与流程挂载**：
    * 支持全局表单（一个流程一个表单）与节点表单（不同审批节点展示不同表单或不同字段级权限）。
* **字段级权限控制**：基于当前任务节点，控制表单字段的“只读”、“必填”、“隐藏”状态。
* **数据绑定**：表单提交的 JSON 数据自动映射为 Flowable 的流程变量（Process Variables），供网关和表达式使用。

### 4. 权限与组织架构 (RBAC & Identity)
* **多维度架构**：支持多租户（Tenant）、多部门（Department/Group）、多角色（Role）。
* **人员与引擎同步**：将系统的用户、角色、部门结构同步或桥接到 Flowable 的 IDM（Identity Management）模块，以便在流程图中直接使用表达式分配任务（如：`${deptManager}`）。
* **数据权限隔离**：确保租户间数据绝对隔离。

### 5. 沟通与备注模块 (Comments & Collaboration)
* **审批意见记录**：每个审批节点的通过、驳回等操作必须提供审批意见输入框。
* **附件管理**：任务和流程级别支持上传文档、图片等附件。
* **@提及与沟通**：支持在审批意见中 @ 相关人员，触发即时通知。

### 6. 审计与日志模块 (Audit & Logging)
* **业务审计日志**：记录谁在什么时间、什么 IP、对什么流程实例进行了什么操作（启动、审批、挂起、终止）。
* **引擎历史归档**：利用 Flowable Native 的 History Service，定期将已完成的实例和变量归档或转移到冷库，保证运行库性能。

### 7. 统计报表 (Analytics & Dashboards)
* **多维度统计图表 (ECharts)**：
    * 个人/部门任务处理效率分析（平均耗时、逾期率）。
    * 流程实例分布图（哪种流程使用最频繁）。
    * 流程瓶颈分析（识别哪个节点的平均停留时间最长）。
* **自定义仪表盘**：支持用户拖拽图表卡片，自定义工作台（Dashboard）。

### 8. 管理界面 (Admin Console)
#### 8.1 模型部署与管理
1.  **完美兼容 Flowable Designer**：支持导入/导出标准的 XML 文件。
2.  **多协议支持**：支持 CMMN（案例管理）、BPMN（业务流程）、DMN（决策表）的统一上传、校验与部署。
3.  **版本管理与回滚**：支持同一流程模型的多个版本共存，支持设定“当前主版本”，以及在新旧版本间的平滑切换。
4.  **在线 XML 预览**：支持在前端直接查看模型定义文件与图形化流程图。

#### 8.2 模型实例管理 (Instance Management)
1.  **状态控制**：对运行中的流程实例进行**挂起（Suspend）**和**激活（Activate）**操作。
2.  **异常干预**：超级管理员可以**强制终止（Terminate）**或**删除**错误的流程实例。
3.  **动态变量修改**：支持在管理后台直接干预并修改运行中流程的全局变量（用于修复因数据错误导致的流程卡死）。
4.  **节点跳转（自由流）**：管理员权限下，支持将当前实例强制跳转到任意指定节点。

#### 8.3 系统与字典管理
1.  **用户、用户组及角色管理**：支持增删改查，以及层级关系的维护。
2.  **数据字典管理**：统一定义表单中下拉框、单选框所需的枚举值（如：请假类型、报销科目），供 SurveyJS 动态加载。

---

## 三、 部署架构

### 1. Docker 容器化部署

平台采用 Docker 容器化技术，实现应用的轻量级、可移植、和环境一致性部署。所有组件（前端、后端、数据库）均封装为独立的 Docker 容器。

#### 1.1 容器化优势

* **环境一致性**：开发、测试、生产环境完全一致，消除"在我机器上能运行"问题
* **快速部署**：一键启动所有服务，大幅缩短部署时间
* **资源隔离**：每个服务独立运行，避免依赖冲突
* **横向扩展**：支持通过 Docker Swarm 或 Kubernetes 进行容器编排和弹性伸缩
* **版本管理**：每个容器镜像版本化，支持快速回滚和升级

#### 1.2 容器镜像组织

```
flowable-platform/
├── backend/                 # 后端服务容器
│   ├── Dockerfile
│   ├── target/*.jar        # Spring Boot 可执行 JAR
│   └── src/main/resources/
│       ├── application.yml # 配置文件
│       └── flowable/       # Flowable 配置
├── frontend/               # 前端应用容器
│   ├── Dockerfile
│   ├── .next/             # Next.js 构建产物
│   ├── public/            # 静态资源
│   └── package.json
├── database/              # 数据库初始化脚本
│   └── init.sql
└── docker-compose.yml     # 多容器编排配置
```

#### 1.3 后端 Dockerfile

```dockerfile
# 多阶段构建：构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段：轻量级运行时镜像
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**关键配置说明**：
* **多阶段构建**：分离构建和运行环境，减小最终镜像体积
* **Java 21 JRE**：使用 Alpine Linux 基础镜像，优化镜像大小
* **健康检查**：定期检测应用健康状态，支持容器编排器自动重启
* **端口暴露**：8080 端口用于 Spring Boot 应用访问
* **非 root 用户**：生产环境建议创建专用运行用户（安全性）

#### 1.4 前端 Dockerfile

```dockerfile
# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 运行阶段：使用 Nginx 提供静态文件服务
FROM nginx:alpine
COPY --from=builder /app/.next /app/.next
COPY --from=builder /app/public /app/public
COPY --from=builder /app/package.json /app/

# 自定义 Nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
```

**关键配置说明**：
* **Next.js 静态导出**：构建完成后使用 Nginx 提供高性能静态文件服务
* **Alpine 镜像**：最小化镜像体积，提升启动速度
* **Nginx 配置**：支持反向代理、负载均衡、HTTPS、缓存策略
* **端口 3000**：前端应用访问端口

#### 1.5 数据库容器

使用官方 PostgreSQL Docker 镜像，通过环境变量进行配置：

```yaml
environment:
  POSTGRES_DB: flowable_platform
  POSTGRES_USER: flowable
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-flowable123}
  POSTGRES_INITDB_ARGS: "-E UTF8 --locale=C"
```

**初始化脚本**：挂载 `./database/init.sql` 到 `/docker-entrypoint-initdb.d/`，容器启动时自动执行表结构和初始数据创建。

---

### 2. Docker Compose 一键编排

通过 `docker-compose.yml` 实现前端、后端、数据库三个服务的统一编排和一键启动。

#### 2.1 完整 docker-compose.yml 配置

```yaml
version: '3.8'

services:
  # PostgreSQL 数据库服务
  postgres:
    image: postgres:15-alpine
    container_name: flowable-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: flowable_platform
      POSTGRES_USER: flowable
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-flowable123}
      POSTGRES_INITDB_ARGS: "-E UTF8 --locale=C"
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U flowable"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - flowable-network

  # Spring Boot 后端服务
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: flowable-backend
    restart: unless-stopped
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/flowable_platform
      SPRING_DATASOURCE_USERNAME: flowable
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-flowable123}
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      FLOWABLE_DATABASE_SCHEMA_UPDATE: true
      JAVA_OPTS: "-Xms512m -Xmx1024m"
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      start_period: 60s
      retries: 3
    networks:
      - flowable-network

  # Next.js 前端服务
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: flowable-frontend
    restart: unless-stopped
    environment:
      NEXT_PUBLIC_API_URL: http://backend:8080
      NODE_ENV: production
    ports:
      - "3000:3000"
    depends_on:
      backend:
        condition: service_healthy
    networks:
      - flowable-network

# 数据卷持久化
volumes:
  postgres_data:
    driver: local

# 网络配置
networks:
  flowable-network:
    driver: bridge
```

#### 2.2 服务依赖关系

```
frontend (前端)
    ↓ depends_on (健康检查)
backend (后端)
    ↓ depends_on (健康检查)
postgres (数据库)
```

**启动顺序**：
1. **PostgreSQL**：数据库最先启动，等待健康检查通过（接受连接）
2. **Backend**：后端服务启动，等待数据库就绪后连接，完成 Flyway 迁移和 Flowable 表创建
3. **Frontend**：前端服务启动，等待后端健康检查通过，确保 API 可用

**健康检查机制**：
- PostgreSQL: `pg_isready` 检测数据库接受连接
- Backend: Spring Boot Actuator `/actuator/health` 端点
- Frontend: HTTP 200 响应检测

#### 2.3 环境变量管理

创建 `.env` 文件管理敏感配置（不提交到版本控制）：

```env
# 数据库配置
POSTGRES_PASSWORD=your_secure_password_here

# Spring Boot 配置
SPRING_PROFILES_ACTIVE=docker
JAVA_OPTS=-Xms512m -Xmx1024m

# Next.js 配置
NEXT_PUBLIC_API_URL=http://localhost:8080
```

**.gitignore 配置**：
```
.env
.env.local
```

---

### 3. 快速启动指南

#### 3.1 前置要求

* **Docker**: 20.10+ 版本
* **Docker Compose**: 2.0+ 版本

**安装验证**：
```bash
docker --version
docker-compose --version
```

#### 3.2 一键启动命令

```bash
# 克隆项目
git clone <repository-url>
cd flowable-platform-demo

# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 访问应用
# 前端：http://localhost:3000
# 后端 API：http://localhost:8080
# 数据库：localhost:5432
```

#### 3.3 服务管理命令

```bash
# 停止所有服务
docker-compose stop

# 启动已停止的服务
docker-compose start

# 重启服务
docker-compose restart

# 停止并删除容器、网络（保留数据卷）
docker-compose down

# 停止并删除容器、网络、数据卷（完全清理）
docker-compose down -v

# 重新构建镜像（代码更新后）
docker-compose build --no-cache

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 进入容器调试
docker-compose exec backend sh
docker-compose exec postgres psql -U flowable -d flowable_platform
```

#### 3.4 生产环境部署优化

**资源配置**：
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
```

**日志驱动配置**：
```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

**健康检查调优**：根据生产环境负载调整健康检查间隔和超时时间。

---

### 4. Docker Compose 扩展配置

#### 4.1 开发环境覆盖 (docker-compose.override.yml)

创建开发环境专用配置，支持热重载和调试：

```yaml
version: '3.8'

services:
  backend:
    volumes:
      - ./backend/src:/app/src:ro  # 挂载源码，支持热重载
    environment:
      SPRING_PROFILES_ACTIVE: dev,docker
      JAVA_DEBUG: "true"
    ports:
      - "5005:5005"  # Java 调试端口

  frontend:
    volumes:
      - ./frontend/src:/app/src:ro
      - ./frontend/public:/app/public:ro
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
      NODE_ENV: development
    command: npm run dev
```

**开发环境启动**：
```bash
docker-compose -f docker-compose.yml -f docker-compose.override.yml up
```

#### 4.2 监控服务扩展

可选添加监控服务（Prometheus + Grafana）：

```yaml
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: flowable-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - flowable-network

  grafana:
    image: grafana/grafana:latest
    container_name: flowable-grafana
    ports:
      - "3001:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD:-admin}
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - flowable-network

volumes:
  prometheus_data:
  grafana_data:
```

---

### 5. 部署验证清单

部署完成后，验证以下检查点：

- [ ] **容器状态**：所有容器状态为 `running` 或 `healthy`
- [ ] **前端访问**：浏览器访问 `http://localhost:3000` 显示登录页面
- [ ] **后端健康检查**：`curl http://localhost:8080/actuator/health` 返回 `{"status":"UP"}`
- [ ] **数据库连接**：后端日志显示成功连接 PostgreSQL，无连接异常
- [ ] **Flowable 引擎**：后端日志显示 Flowable 表初始化成功
- [ ] **API 调用**：前端能够成功调用后端 API（检查浏览器 Network 面板）
- [ ] **日志输出**：`docker-compose logs` 无 ERROR 级别日志
- [ ] **数据持久化**：重启容器后数据不丢失（PostgreSQL 数据卷挂载正常）

---

### 6. 故障排查

#### 6.1 常见问题

**问题 1：容器启动失败**
```bash
# 查看详细错误日志
docker-compose logs backend

# 常见原因：
# - 端口冲突（修改 docker-compose.yml 中的端口映射）
# - 镜像构建失败（检查 Dockerfile 语法和依赖）
# - 内存不足（调整 JAVA_OPTS 内存配置）
```

**问题 2：数据库连接失败**
```bash
# 检查数据库容器健康状态
docker-compose ps postgres

# 进入后端容器测试连接
docker-compose exec backend sh
ping postgres
telnet postgres 5432

# 常见原因：
# - 数据库未就绪（增加 depends_on 健康检查等待时间）
# - 网络隔离问题（确保在同一网络）
# - 密码错误（检查 .env 文件配置）
```

**问题 3：前端无法访问后端 API**
```bash
# 检查网络连通性
docker-compose exec frontend ping backend

# 检查环境变量配置
docker-compose exec frontend env | grep API_URL

# 常见原因：
# - API_URL 配置错误（开发用 localhost，生产用 backend 服务名）
# - CORS 跨域配置问题（后端 Spring Security 配置）
# - 后端健康检查未通过（前端启动过早）
```

#### 6.2 调试技巧

```bash
# 实时查看多服务日志
docker-compose logs -f backend frontend

# 进入容器 Shell 调试
docker-compose exec backend sh
docker-compose exec postgres psql -U flowable -d flowable_platform

# 查看容器资源占用
docker stats

# 重启单个服务
docker-compose restart backend

# 完全重建（清理所有数据，重新开始）
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

### 7. 生产环境部署建议

#### 7.1 安全加固

* **镜像扫描**：使用 Trivy 扫描镜像漏洞
* **非 root 用户**：所有容器使用非特权用户运行
* **Secret 管理**：使用 Docker Secrets 或外部 Vault 管理敏感信息
* **网络隔离**：使用私有网络，仅暴露必要端口
* **TLS/HTTPS**：前端使用 Nginx 配置 SSL 证书

#### 7.2 高可用部署

对于生产环境，建议使用 **Kubernetes** 替代 Docker Compose：

* **自动故障恢复**：Pod 崩溃自动重启
* **滚动更新**：零停机部署
* **弹性伸缩**：根据负载自动扩缩容
* **服务发现**：内置服务注册和发现机制
* **配置管理**：ConfigMap 和 Secret 管理

#### 7.3 备份策略

* **数据库备份**：定期备份 PostgreSQL 数据卷
* **配置备份**：版本控制 docker-compose.yml 和环境配置
* **镜像备份**：私有镜像仓库（Docker Hub/阿里云 ACR）

---

### 8. 性能优化建议

* **镜像优化**：使用 Alpine 基础镜像，多阶段构建减小体积
* **启动优化**：减少依赖下载，使用本地镜像缓存
* **网络优化**：使用自定义网络，避免 DNS 解析延迟
* **资源限制**：合理配置 CPU 和内存限制，防止资源争抢
* **连接池配置**：后端数据库连接池大小与容器数量匹配

---

**总结**：通过 Docker Compose，实现"一个命令启动整个平台"的简化部署体验，大幅降低运维复杂度，提升开发效率和部署一致性。
