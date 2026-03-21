# Research: Flowable Platform Core

**Date**: 2026-03-21
**Purpose**: Technical research and decision documentation for Flowable Platform Core implementation
**Status**: Complete

---

## 1. Flowable 7.x Multi-Tenant Configuration

### Decision: Use tenant_id Column Strategy

**Rationale**:
- Flowable 7.x natively supports multi-tenancy through tenant_id columns in core tables
- Single database schema simplifies deployment and migration compared to separate schemas per tenant
- Tenant_id column filtering provides sufficient isolation while maintaining query performance
- Flowable's IdentityService handles tenant-aware user/group/role resolution

**Implementation**:
- Configure Flowable with `flowable.database-schema-update: true` for auto-DDL
- Set `flowable.tenant-id-column-tenant-value: true` to enable multi-tenant
- All custom queries must include `WHERE tenant_id = :tenantId` clause
- Cache keys must prefix with tenant identifier: `tenant:{tenantId}:cacheKey`

**Alternatives Considered**:
- **Separate schemas per tenant**: Rejected due to complexity in migration and Flowable engine configuration
- **Separate databases per tenant**: Rejected due to operational overhead and resource waste

**Configuration Example**:
```yaml
flowable:
  database-schema-update: true
  tenant-id-column-tenant-value: true
  id-engine:
    enabled: true
    use-new-idm-engine: true
```

---

## 2. Spring Boot 3.5.x + Flowable 7.x Integration

### Decision: Use flowable-spring-boot-starter

**Rationale**:
- Spring Boot 3.5.x requires Jakarta EE 9+ (javax.* namespace migration)
- Flowable 7.x provides official Spring Boot starter with auto-configuration
- Starter handles bean initialization, transaction management, and engine configuration
- Supports Spring 6.x security integration out of the box

**Implementation**:
```xml
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter-rest</artifactId>
    <version>7.0.0</version>
</dependency>
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter-actuator</artifactId>
    <version>7.0.0</version>
</dependency>
```

**Service Injection Pattern**:
```java
@Service
public class ProcessService {
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    // Direct injection - no abstraction layers
    public ProcessService(RuntimeService runtimeService,
                         TaskService taskService,
                         HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
    }
}
```

**Alternatives Considered**:
- **Manual Flowable configuration**: Rejected due to complexity and error-prone setup
- **Custom abstraction layer**: Rejected - violates constitution principle II

---

## 3. Next.js App Router + SurveyJS Integration

### Decision: Server Component Wrapper + Client Component Form Renderer

**Rationale**:
- Next.js 14+ App Router uses Server Components by default (no client-side JS)
- SurveyJS React library requires client-side rendering ('use client' directive)
- Hybrid approach: Server Component fetches form schema and passes to Client Component wrapper

**Implementation**:
```typescript
// Server Component (app/tasks/[id]/page.tsx)
export default async function TaskPage({ params }) {
  const task = await fetchTask(params.id); // Server-side data fetching
  const formSchema = await fetchFormSchema(task.formSchemaId);

  return (
    <div>
      <h1>{task.name}</h1>
      <SurveyFormRenderer
        formSchema={formSchema}
        taskId={task.id}
        onSubmit={handleFormSubmit} // Server Action
      />
    </div>
  );
}

// Client Component (components/forms/SurveyFormRenderer.tsx)
'use client';
import { Survey } from 'surveyjs-react-ui';

export function SurveyFormRenderer({ formSchema, taskId, onSubmit }) {
  const survey = new Survey.Model(formSchema);

  return (
    <Survey
      model={survey}
      onComplete={onSubmit}
    />
  );
}
```

**Form Version Control**:
- FormSchema entity stores JSON schema with semantic versioning
- ProcessInstance records formSchemaId used at start time
- Task rendering queries form by processInstance.formSchemaId
- Ensures historical instances always render with original form version

**Alternatives Considered**:
- **Full client-side rendering**: Rejected - loses Next.js Server Component benefits
- **Static form generation**: Rejected - doesn't support dynamic form builder requirements

---

## 4. OAuth2 + OpenID Connect Implementation

### Decision: Spring Security 6.x OAuth2 Client + Multiple Providers

**Rationale**:
- Spring Security 6.x provides built-in OAuth2/OIDC client support
- Multiple identity providers supported per tenant (Azure AD, Okta, Google)
- Fallback to local username/password authentication
- JWT-based session management for scalability

**Implementation**:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          azure:
            client-id: ${AZURE_CLIENT_ID}
            client-secret: ${AZURE_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
        provider:
          azure:
            authorization-uri: https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/authorize
            token-uri: https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/token
            user-info-uri: https://graph.microsoft.com/v1.0/me
```

**Tenant-Specific Configuration**:
- Tenant entity stores oauthProvider and oauthClientId
- Authentication flow reads provider from tenant context
- Dynamic client registration per tenant

**Local Auth Fallback**:
```java
@Bean
public UserDetailsService userDetailsService(UserRepository userRepository) {
    return username -> userRepository
        .findByUsernameAndTenantId(username, getCurrentTenantId())
        .map(user -> User.withUsername(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRoles().stream()
                .map(Role::getCode)
                .toArray(String[]::new))
            .build())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
}
```

---

## 5. bpmn.js Integration with Next.js

### Decision: Client Component with Dynamic Module Loading

**Rationale**:
- bpmn.js is a large library (~500KB) and should be code-split
- Load only when process diagram is viewed
- Use dynamic import() to lazy-load bpmn-navigated-viewer component

**Implementation**:
```typescript
// Client Component with dynamic import
'use client';
import dynamic from 'next/dynamic';

const BpmnViewer = dynamic(
  () => import('bpmn-js/lib/NavigatedViewer'),
  {
    ssr: false,
    loading: () => <p>Loading diagram...</p>
  }
);

export function ProcessDiagram({ xml }) {
  return (
    <div className="diagram-container">
      <BpmnViewer
        xml={xml}
        additionalModules={[currentNodeHighlightModule]}
      />
    </div>
  );
}
```

**Current Node Highlighting**:
```javascript
import ModelingModule from 'bpmn-js/lib/features/modeling';
import { is } from 'bpmn-js/lib/util/ModelUtil';

const currentNodeHighlightModule = {
  __init__: ['highlightCurrentNode'],
  highlightCurrentNode: ['eventBus', 'canvas', 'elementRegistry', function(eventBus, canvas, elementRegistry) {
    eventBus.on('element.changed', function(event) {
      const element = event.element;
      if (is(element, 'bpmn:Task') && element.businessObject.$model.ids.indexOf(currentTaskId) >= 0) {
        canvas.addMarker(element.id, 'highlight-current');
      }
    });
  }]
};
```

**Alternatives Considered**:
- **Server-side rendering**: Rejected - bpmn.js requires browser DOM
- **Pre-bundled library**: Rejected - increases initial bundle size unnecessarily

---

## 6. PostgreSQL Multi-Tenant Query Optimization

### Decision: Composite Indexes on tenant_id + Frequently Filtered Columns

**Rationale**:
- All queries must filter by tenant_id for data isolation
- Composite indexes (tenant_id, other_column) optimize filtering and sorting
- PostgreSQL 15+ includes query plan optimization for multi-tenant patterns

**Index Strategy**:
```sql
-- User table
CREATE INDEX idx_user_tenant_username ON users(tenant_id, username);
CREATE INDEX idx_user_tenant_email ON users(tenant_id, email);
CREATE INDEX idx_user_tenant_active ON users(tenant_id, is_active);

-- Task queries (Flowable tables)
CREATE INDEX idx_act_ru_task_tenant ON act_ru_task(tenant_id_, assignee_);
CREATE INDEX idx_act_ru_task_tenant_candidate ON act_ru_task(tenant_id_, candidate_);
CREATE INDEX idx_act_ru_task_tenant_create ON act_ru_task(tenant_id_, create_time_);

-- Process instances
CREATE INDEX idx_act_ru_execution_tenant ON act_ru_execution(tenant_id_, start_time_);
CREATE INDEX idx_act_hi_procinst_tenant ON act_hi_procinst(tenant_id_, end_time_);
```

**Query Optimization**:
- Use prepared statements with parameterized tenant_id
- Enable PostgreSQL query plan cache: `statement_cache_size = 1000`
- Partitioning strategy: Consider table partitioning by tenant_id if tenant count > 100

---

## 7. Testcontainers for Flowable Integration Testing

### Decision: Testcontainers PostgreSQL Module + Flowable Engine Configuration

**Rationale**:
- Testcontainers provides real PostgreSQL in Docker for integration tests
- Eliminates need for mocking Flowable services (violates constitution)
- Tests run against actual Flowable engine behavior
- Fast test execution with container reuse

**Implementation**:
```java
@SpringBootTest
@Testcontainers
public class ProcessIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("flowable_test")
        .withUsername("flowable")
        .withPassword("flowable");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldCompleteApprovalProcess() {
        // Deploy BPMN process
        repositoryService.createDeployment()
            .addClasspathResource("processes/approval.bpmn20.xml")
            .deploy();

        // Start process instance
        ProcessInstance process = runtimeService.startProcessInstanceByKey(
            "approval",
            Map.of("tenantId", TEST_TENANT_ID)
        );

        // Verify task created
        List<Task> tasks = taskService.createTaskQuery()
            .taskDefinitionKey("approve")
            .list();

        assertThat(tasks).hasSize(1);

        // Complete task
        taskService.complete(tasks.get(0).getId());

        // Verify process completed
        ProcessInstance completed = runtimeService.createProcessInstanceQuery()
            .processInstanceId(process.getId())
            .singleResult();

        assertThat(completed).isNull();
    }
}
```

**Test Data Setup**:
- Use `@Sql` annotation to execute test SQL before each test
- Clean up with `@Transactional` rollback after each test
- Isolate tenant data using unique test tenant IDs

---

## Summary

All technical unknowns have been resolved through research. Key decisions:

1. **Multi-tenancy**: tenant_id column strategy with comprehensive indexing
2. **Flowable Integration**: Direct native service usage, no abstractions
3. **Frontend**: Server Components + selective Client Components for interactivity
4. **Authentication**: OAuth2/OIDC with local fallback, tenant-aware
5. **Process Visualization**: Lazy-loaded bpmn.js with current node highlighting
6. **Database**: PostgreSQL 15+ with composite tenant_id indexes
7. **Testing**: Testcontainers for real integration testing

**Ready for Phase 1 design and implementation**.
