# Data Model: Backend Testing Infrastructure

**Feature**: Backend Testing Infrastructure (002-backend-testing)
**Date**: 2026-03-22
**Status**: Complete

## Overview

This document defines the test data model, test entity structures, and test fixtures for the backend testing infrastructure. Unlike production data models, test data models are focused on test isolation, repeatability, and comprehensive coverage of edge cases.

## Test Entities

### 1. Test Tenant

**Purpose**: Dedicated tenant for testing multi-tenant isolation and data separation.

**Attributes**:
- `tenant_id`: UUID (fixed value for test tenant)
- `tenant_name`: "Test Tenant"
- `tenant_schema`: "test_tenant"
- `is_active`: true

**Creation**: Created via Flyway migration `V__create_test_tenant.sql`

**Usage**:
- All API tests run in context of test tenant
- Tenant context established via @BeforeEach hook
- Validates multi-tenant data isolation

### 2. Test User

**Purpose**: Pre-configured test users for authentication and authorization testing.

**Attributes**:
- `user_id`: UUID (fixed values for different test user types)
- `username`: String ("test-admin", "test-user", "test-guest")
- `password`: String (hashed, known plaintext for testing)
- `email`: String
- `role`: String ("ADMIN", "USER", "GUEST")
- `tenant_id`: UUID (test tenant)

**Test User Types**:
1. **test-admin**: Full administrative permissions
2. **test-user**: Standard user permissions
3. **test-guest**: Limited/guest permissions
4. **test-inactive**: Inactive user (for negative testing)

**Creation**: Created via fixture SQL scripts

### 3. Test Process Definition

**Purpose**: Simplified process definitions for testing Flowable integration.

**Attributes**:
- `process_definition_key`: String (e.g., "test-simple-process")
- `process_name`: String
- `version`: Integer
- `deployment_id`: String
- `tenant_id`: UUID (test tenant)

**Test Process Types**:
1. **test-simple-process**: Single user task, no gateways
2. **test-parallel-process**: Parallel gateway, multiple tasks
3. **test-conditional-process**: Exclusive gateway with conditions
4. **test-timer-process**: Timer event for time-based testing

**Creation**: Deployed via test setup @BeforeAll

### 4. Test Task

**Purpose**: Pre-configured tasks for testing task management endpoints.

**Attributes**:
- `task_id`: String
- `task_name`: String
- `task_definition_key`: String
- `process_instance_id`: String
- `assignee`: String (test user)
- `tenant_id`: UUID (test tenant)
- `status`: String ("ACTIVE", "COMPLETED", "CANCELLED")

**Test Task Scenarios**:
1. Assigned task (assigned to test-user)
2. Unassigned task (no assignee)
3. Completed task (for history queries)
4. Claimed task (claimed by test-user)

**Creation**: Created via test setup or process instances

### 5. Test Form Schema

**Purpose**: Form schemas for testing form-process binding and validation.

**Attributes**:
- `form_schema_id`: UUID
- `form_name`: String
- `form_version`: Integer
- `process_definition_key`: String
- `task_definition_key`: String
- `schema_json`: JSON (SurveyJS schema)
- `tenant_id`: UUID (test tenant)

**Test Form Types**:
1. **test-simple-form**: Basic text fields, validation
2. **test-complex-form**: Multiple sections, conditional fields
3. **test-validation-form**: All validation types (required, regex, min/max)

**Creation**: Created via fixture JSON files

## Test Fixtures

### Fixture Files Location

```
backend/src/test/resources/fixtures/
├── sql/
│   ├── test_users.sql           # Test user data
│   ├── test_form_schemas.sql    # Test form schemas
│   └── test_tasks.sql           # Test task data
├── json/
│   ├── test_simple_form.json    # Simple form schema
│   ├── test_complex_form.json   # Complex form schema
│   └── test_validation_form.json # Validation form schema
└── processes/
    ├── test-simple-process.bpmn20.xml
    ├── test-parallel-process.bpmn20.xml
    └── test-conditional-process.bpmn20.xml
```

### Fixture: Test Users

**File**: `backend/src/test/resources/fixtures/sql/test_users.sql`

```sql
-- Test Admin User
INSERT INTO users (user_id, username, password_hash, email, role, tenant_id, is_active)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'test-admin',
  '$2a$10$test.admin.hashed.password',
  'test-admin@example.com',
  'ADMIN',
  '00000000-0000-0000-0000-000000000001', -- test tenant
  true
);

-- Test Regular User
INSERT INTO users (user_id, username, password_hash, email, role, tenant_id, is_active)
VALUES (
  '22222222-2222-2222-2222-222222222222',
  'test-user',
  '$2a$10$test.user.hashed.password',
  'test-user@example.com',
  'USER',
  '00000000-0000-0000-0000-000000000001', -- test tenant
  true
);

-- Test Guest User
INSERT INTO users (user_id, username, password_hash, email, role, tenant_id, is_active)
VALUES (
  '33333333-3333-3333-3333-333333333333',
  'test-guest',
  '$2a$10$test.guest.hashed.password',
  'test-guest@example.com',
  'GUEST',
  '00000000-0000-0000-0000-000000000001', -- test tenant
  true
);

-- Test Inactive User
INSERT INTO users (user_id, username, password_hash, email, role, tenant_id, is_active)
VALUES (
  '44444444-4444-4444-4444-444444444444',
  'test-inactive',
  '$2a$10$test.inactive.hashed.password',
  'test-inactive@example.com',
  'USER',
  '00000000-0000-0000-0000-000000000001', -- test tenant
  false
);
```

### Fixture: Test Form Schema

**File**: `backend/src/test/resources/fixtures/json/test_simple_form.json`

```json
{
  "formName": "Test Simple Form",
  "formVersion": 1,
  "processDefinitionKey": "test-simple-process",
  "taskDefinitionKey": "test-user-task",
  "schema": {
    "pages": [
      {
        "name": "page1",
        "elements": [
          {
            "type": "text",
            "name": "firstName",
            "title": "First Name",
            "isRequired": true,
            "validators": [
              {
                "type": "regex",
                "text": "Only letters allowed",
                "pattern": "^[a-zA-Z]+$"
              }
            ]
          },
          {
            "type": "text",
            "name": "lastName",
            "title": "Last Name",
            "isRequired": true
          },
          {
            "type": "comment",
            "name": "comments",
            "title": "Comments"
          }
        ]
      }
    ]
  }
}
```

## Test Data Builder Pattern

### Purpose

Builder classes provide fluent API for creating test entities in tests. Builders encapsulate fixture creation logic and provide defaults for common test scenarios.

### Example: TestDataBuilder

```java
public class TestDataBuilder {
    private UUID userId;
    private String username;
    private String role;
    private UUID tenantId;

    public static TestDataBuilder aUser() {
        return new TestDataBuilder()
            .withUserId(UUID.randomUUID())
            .withUsername("test-user")
            .withRole("USER")
            .withTenantId(TestTenantConfig.TEST_TENANT_ID);
    }

    public TestDataBuilder withUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public TestDataBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public TestDataBuilder withRole(String role) {
        this.role = role;
        return this;
    }

    public TestDataBuilder asAdmin() {
        this.role = "ADMIN";
        return this;
    }

    public TestDataBuilder asGuest() {
        this.role = "GUEST";
        return this;
    }

    public User build() {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setRole(role);
        user.setTenantId(tenantId);
        return user;
    }
}
```

### Usage in Tests

```java
@Test
void shouldCreateUser() {
    // Given
    User testUser = TestDataBuilder.aUser()
        .withUsername("john.doe")
        .withEmail("john@example.com")
        .build();

    // When
    userService.create(testUser);

    // Then
    User found = userRepository.findById(testUser.getUserId());
    assertThat(found).isNotNull();
    assertThat(found.getUsername()).isEqualTo("john.doe");
}
```

## Test Data Lifecycle

### Setup (@BeforeEach)

1. **Establish tenant context**: Set ThreadLocal tenant_id to test tenant
2. **Load fixtures**: Execute SQL fixtures via @Sql or FixtureLoader
3. **Create test data**: Use builders for dynamic test data
4. **Deploy processes**: Deploy test process definitions via Flowable repository service

### Execution (Test Method)

1. **Execute test logic**: Call system under test
2. **Verify results**: Assert expected state
3. **Validate multi-tenancy**: Verify tenant_id is correct

### Cleanup (@AfterEach)

1. **Transactional rollback**: Automatic via @Transactional
2. **Reset tenant context**: Clear ThreadLocal tenant_id
3. **Clean up test data**: Not needed due to rollback

## Test Data Validation

### Multi-Tenant Isolation Tests

```java
@Test
void shouldNotAccessOtherTenantData() {
    // Given
    UUID tenant1 = TestTenantConfig.TEST_TENANT_ID;
    UUID tenant2 = UUID.randomUUID(); // different tenant

    TestDataBuilder.aUser().withTenantId(tenant1).build();
    TestDataBuilder.aUser().withTenantId(tenant2).build();

    // When - tenant1 context
    TestTenantContext.setTenantId(tenant1);
    List<User> tenant1Users = userRepository.findAll();

    // Then - should only see tenant1 users
    assertThat(tenant1Users).hasSize(1);
    assertThat(tenant1Users.get(0).getTenantId()).isEqualTo(tenant1);
}
```

### Data Consistency Tests

```java
@Test
void shouldMaintainReferentialIntegrity() {
    // Given
    Task task = TestDataBuilder.aTask()
        .withProcessInstanceId(processInstance.getId())
        .build();

    // When - complete task
    taskService.complete(task.getTaskId());

    // Then - verify process instance completed
    HistoricProcessInstance hpi = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertThat(hpi.getEndActivityId()).isNotNull();
}
```

## Next Steps

With test data model defined, proceed to:
1. **contracts/**: Define test helper interfaces and base test classes
2. **quickstart.md**: Create developer guide for writing and running tests
