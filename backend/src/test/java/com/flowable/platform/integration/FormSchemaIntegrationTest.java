package com.flowable.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowable.platform.config.AbstractIntegrationTest;
import com.flowable.platform.entity.FormSchema;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.repository.FormSchemaRepository;
import com.flowable.platform.repository.TenantRepository;
import org.flowable.engine.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FormSchemaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FormSchemaRepository formSchemaRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        // Create test tenant
        testTenant = new Tenant("Test Tenant", "test-tenant");
        testTenant.setIsActive(true);
        testTenant = tenantRepository.save(testTenant);
    }

    @Test
    @Transactional
    void testCreateFormSchema() throws Exception {
        FormSchema formSchema = new FormSchema();
        formSchema.setTenant(testTenant);
        formSchema.setName("Leave Request Form");
        formSchema.setDescription("Form for leave requests");
        formSchema.setVersion(1);
        formSchema.setSchema(createTestSchema());
        formSchema.setIsActive(true);
        formSchema.setCreatedAt(LocalDateTime.now());
        formSchema.setUpdatedAt(LocalDateTime.now());

        FormSchema savedForm = formSchemaRepository.save(formSchema);

        assertNotNull(savedForm);
        assertNotNull(savedForm.getId());
        assertEquals("Leave Request Form", savedForm.getName());
        assertEquals(1, savedForm.getVersion());
        assertTrue(savedForm.isActive());
    }

    @Test
    @Transactional
    void testFormSchemaVersioning() throws Exception {
        // Create initial form schema
        FormSchema formSchemaV1 = new FormSchema();
        formSchemaV1.setTenant(testTenant);
        formSchemaV1.setName("Leave Request Form");
        formSchemaV1.setVersion(1);
        formSchemaV1.setSchema(createTestSchema());
        formSchemaV1.setIsActive(true);
        formSchemaV1.setCreatedAt(LocalDateTime.now());
        formSchemaV1.setUpdatedAt(LocalDateTime.now());
        formSchemaV1 = formSchemaRepository.save(formSchemaV1);

        // Create updated version
        FormSchema formSchemaV2 = new FormSchema();
        formSchemaV2.setTenant(testTenant);
        formSchemaV2.setName("Leave Request Form");
        formSchemaV2.setVersion(2);
        formSchemaV2.setSchema(createUpdatedTestSchema());
        formSchemaV2.setIsActive(true);
        formSchemaV2.setCreatedAt(LocalDateTime.now());
        formSchemaV2.setUpdatedAt(LocalDateTime.now());
        formSchemaV2 = formSchemaRepository.save(formSchemaV2);

        // Verify both versions exist
        List<FormSchema> allVersions = formSchemaRepository.findByTenantIdAndName(
                testTenant.getId(), "Leave Request Form");

        assertEquals(2, allVersions.size());
        assertTrue(allVersions.stream().anyMatch(f -> f.getVersion() == 1));
        assertTrue(allVersions.stream().anyMatch(f -> f.getVersion() == 2));
    }

    @Test
    @Transactional
    void testFormToProcessVariableMapping() throws Exception {
        // Create form schema
        FormSchema formSchema = new FormSchema();
        formSchema.setTenant(testTenant);
        formSchema.setName("Approval Form");
        formSchema.setVersion(1);
        formSchema.setSchema(createTestSchema());
        formSchema.setIsActive(true);
        formSchema.setCreatedAt(LocalDateTime.now());
        formSchema.setUpdatedAt(LocalDateTime.now());
        formSchema = formSchemaRepository.save(formSchema);

        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Annual leave");
        variables.put("formData", formSchema.getSchema()); // Form schema as JSON string

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Verify form data is stored in process variables
        Map<String, Object> processVariables = runtimeService.getVariables(processInstance.getId());
        assertTrue(processVariables.containsKey("formData"));
        assertNotNull(processVariables.get("formData"));
    }

    @Test
    @Transactional
    void testFormSchemaValidation() throws Exception {
        FormSchema formSchema = new FormSchema();
        formSchema.setTenant(testTenant);
        formSchema.setName("Validated Form");
        formSchema.setVersion(1);
        formSchema.setSchema(createTestSchemaWithValidation());
        formSchema.setIsActive(true);
        formSchema.setCreatedAt(LocalDateTime.now());
        formSchema.setUpdatedAt(LocalDateTime.now());
        formSchema = formSchemaRepository.save(formSchema);

        // Verify schema contains validation rules
        Map<String, Object> schema = objectMapper.readValue(formSchema.getSchema(), Map.class);
        assertTrue(schema.containsKey("validationRules"));

        @SuppressWarnings("unchecked")
        Map<String, Object> validationRules = (Map<String, Object>) schema.get("validationRules");
        assertTrue(validationRules.containsKey("requiredFields"));
        assertTrue(validationRules.containsKey("fieldTypes"));
    }

    @Test
    @Transactional
    void testFormSchemaSizeLimit() throws Exception {
        // Create schema within size limit (<10KB)
        FormSchema formSchema = new FormSchema();
        formSchema.setTenant(testTenant);
        formSchema.setName("Small Form");
        formSchema.setVersion(1);
        formSchema.setSchema(createTestSchema());
        formSchema.setIsActive(true);
        formSchema.setCreatedAt(LocalDateTime.now());
        formSchema.setUpdatedAt(LocalDateTime.now());

        FormSchema savedForm = formSchemaRepository.save(formSchema);
        assertNotNull(savedForm);

        // Verify schema size is within limits
        String schema = savedForm.getSchema();
        assertTrue(schema.length() <= 10000, "Form schema exceeds 10KB limit");
    }

    @Test
    @Transactional
    void testFieldLevelPermissions() throws Exception {
        FormSchema formSchema = new FormSchema();
        formSchema.setTenant(testTenant);
        formSchema.setName("Permission Form");
        formSchema.setVersion(1);
        formSchema.setSchema(createTestSchemaWithPermissions());
        formSchema.setIsActive(true);
        formSchema.setCreatedAt(LocalDateTime.now());
        formSchema.setUpdatedAt(LocalDateTime.now());
        formSchema = formSchemaRepository.save(formSchema);

        // Verify field-level permissions are stored
        Map<String, Object> schema = objectMapper.readValue(formSchema.getSchema(), Map.class);
        assertTrue(schema.containsKey("fieldPermissions"));

        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> fieldPermissions =
                (Map<String, Map<String, String>>) schema.get("fieldPermissions");

        assertTrue(fieldPermissions.containsKey("employeeName"));
        assertEquals("read-only", fieldPermissions.get("employeeName").get("permission"));
    }

    @Test
    @Transactional
    void testHistoricalFormRetrieval() throws Exception {
        // Create form version 1
        FormSchema formV1 = new FormSchema();
        formV1.setTenant(testTenant);
        formV1.setName("Historical Form");
        formV1.setVersion(1);
        formV1.setSchema(createTestSchema());
        formV1.setIsActive(true);
        formV1.setCreatedAt(LocalDateTime.now());
        formV1.setUpdatedAt(LocalDateTime.now());
        formV1 = formSchemaRepository.save(formV1);

        // Start process instance with version 1
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("formId", formV1.getId());
        variables.put("formVersion", 1);
        variables.put("employeeName", "John Doe"); // Add missing variable

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Create form version 2
        FormSchema formV2 = new FormSchema();
        formV2.setTenant(testTenant);
        formV2.setName("Historical Form");
        formV2.setVersion(2);
        formV2.setSchema(createUpdatedTestSchema());
        formV2.setIsActive(true);
        formV2.setCreatedAt(LocalDateTime.now());
        formV2.setUpdatedAt(LocalDateTime.now());
        formSchemaRepository.save(formV2);

        // Retrieve historical form version from process instance
        Integer formVersion = (Integer) runtimeService.getVariable(processInstance.getId(), "formVersion");
        assertEquals(1, formVersion);
    }

    @Test
    @Transactional
    void testFormSchemaTenantIsolation() throws Exception {
        // Create tenant 1
        Tenant tenant1 = new Tenant("Tenant 1", "tenant-1");
        tenant1.setIsActive(true);
        tenant1 = tenantRepository.save(tenant1);

        // Create tenant 2
        Tenant tenant2 = new Tenant("Tenant 2", "tenant-2");
        tenant2.setIsActive(true);
        tenant2 = tenantRepository.save(tenant2);

        // Create form for tenant 1
        FormSchema form1 = new FormSchema();
        form1.setTenant(tenant1);
        form1.setName("Shared Form");
        form1.setVersion(1);
        form1.setSchema(createTestSchema());
        form1.setIsActive(true);
        form1.setCreatedAt(LocalDateTime.now());
        form1.setUpdatedAt(LocalDateTime.now());
        form1 = formSchemaRepository.save(form1);

        // Create form for tenant 2
        FormSchema form2 = new FormSchema();
        form2.setTenant(tenant2);
        form2.setName("Shared Form");
        form2.setVersion(1);
        form2.setSchema(createTestSchema());
        form2.setIsActive(true);
        form2.setCreatedAt(LocalDateTime.now());
        form2.setUpdatedAt(LocalDateTime.now());
        form2 = formSchemaRepository.save(form2);

        // Verify tenant isolation
        List<FormSchema> tenant1Forms = formSchemaRepository.findByTenantId(tenant1.getId());
        List<FormSchema> tenant2Forms = formSchemaRepository.findByTenantId(tenant2.getId());

        assertEquals(1, tenant1Forms.size());
        assertEquals(1, tenant2Forms.size());
        assertNotEquals(tenant1Forms.get(0).getId(), tenant2Forms.get(0).getId());
    }

    // Helper methods

    private String createTestSchema() throws Exception {
        Map<String, Object> schema = new HashMap<>();
        schema.put("title", "Leave Request");
        schema.put("description", "Submit a leave request");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "employeeName");
        field1.put("type", "text");
        field1.put("label", "Employee Name");
        field1.put("required", true);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "reason");
        field2.put("type", "textarea");
        field2.put("label", "Reason");
        field2.put("required", true);

        fields.add(field1);
        fields.add(field2);
        schema.put("fields", fields);

        return objectMapper.writeValueAsString(schema);
    }

    private String createUpdatedTestSchema() throws Exception {
        Map<String, Object> schema = new HashMap<>();
        schema.put("title", "Leave Request");
        schema.put("description", "Submit a leave request (updated)");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "employeeName");
        field1.put("type", "text");
        field1.put("label", "Employee Name");
        field1.put("required", true);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "reason");
        field2.put("type", "textarea");
        field2.put("label", "Reason");
        field2.put("required", true);

        Map<String, Object> field3 = new HashMap<>();
        field3.put("name", "duration");
        field3.put("type", "number");
        field3.put("label", "Duration (days)");
        field3.put("required", true);

        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        schema.put("fields", fields);

        return objectMapper.writeValueAsString(schema);
    }

    private String createTestSchemaWithValidation() throws Exception {
        Map<String, Object> schema = new HashMap<>();
        schema.put("title", "Validated Form");

        Map<String, Object> validationRules = new HashMap<>();
        validationRules.put("requiredFields", Arrays.asList("employeeName", "reason"));
        validationRules.put("fieldTypes", Map.of(
                "employeeName", "text",
                "reason", "textarea"
        ));

        schema.put("validationRules", validationRules);
        return objectMapper.writeValueAsString(schema);
    }

    private String createTestSchemaWithPermissions() throws Exception {
        Map<String, Object> schema = new HashMap<>();
        schema.put("title", "Permission Form");

        Map<String, Object> fieldPermissions = new HashMap<>();
        fieldPermissions.put("employeeName", Map.of(
                "permission", "read-only",
                "roles", Arrays.asList("user", "manager")
        ));
        fieldPermissions.put("reason", Map.of(
                "permission", "editable",
                "roles", Arrays.asList("manager")
        ));

        schema.put("fieldPermissions", fieldPermissions);
        return objectMapper.writeValueAsString(schema);
    }
}
