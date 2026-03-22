# Test Fixtures

## Directory Structure

```
fixtures/
├── sql/           # SQL fixture scripts
│   └── test_users.sql    # Pre-configured test users (admin, user, guest, inactive)
├── json/          # JSON fixture files
│   └── test_simple_form.json  # SurveyJS form schema for testing
└── README.md      # This file
```

## Test Data Builder Examples

Prefer `TestDataBuilder` over raw SQL fixtures for dynamic test data:

```java
// Create a standard user
User user = TestDataBuilder.aUser()
    .withUsername("john.doe")
    .withEmail("john@example.com")
    .buildUser();

// Create an admin
User admin = TestDataBuilder.anAdmin().buildUser();

// Create a guest
User guest = TestDataBuilder.aGuest().buildUser();

// Create an inactive user (for negative testing)
User inactive = TestDataBuilder.anInactiveUser().buildUser();
```

## SQL Fixtures

SQL fixtures are loaded via `@Sql` annotation or `FixtureLoader`:

```java
@Test
@Sql("/fixtures/sql/test_users.sql")
void shouldFindUsers() {
    // test_users.sql is executed before this test
}
```

## JSON Fixtures

JSON fixtures are loaded via `FixtureLoader`:

```java
@Autowired
private FixtureLoader fixtureLoader;

@Test
void shouldLoadFormSchema() {
    FormSchema schema = fixtureLoader.loadJson(
        "fixtures/json/test_simple_form.json",
        FormSchema.class
    );
    assertThat(schema.getFormName()).isEqualTo("Test Simple Form");
}
```

## Pre-configured Test Users

| Username | Role | Tenant | Active | Password |
|----------|------|--------|--------|----------|
| test-admin | ADMIN | test-tenant | true | test-admin-password |
| test-user | USER | test-tenant | true | test-user-password |
| test-guest | GUEST | test-tenant | true | test-guest-password |
| test-inactive | USER | test-tenant | false | test-inactive-password |
