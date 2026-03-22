package com.flowable.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowable.platform.entity.FormSchema;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.repository.FormSchemaRepository;
import com.flowable.platform.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing dynamic form schemas with version control,
 * process variable mapping, validation, and field-level permissions.
 */
@Service
@Transactional
public class FormService {

    private final FormSchemaRepository formSchemaRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormService(
            FormSchemaRepository formSchemaRepository,
            TenantRepository tenantRepository,
            AuditService auditService) {
        this.formSchemaRepository = formSchemaRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a new form schema
     */
    public FormSchema createFormSchema(String tenantCode, String name, String description,
                                         String schema, String validationRules, String fieldPermissions,
                                         String createdBy) {
        // Validate schema size
        validateSchemaSize(schema);

        // Validate schema JSON structure
        validateSchemaJson(schema);

        // Get tenant
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantCode));

        // Create form schema with version 1
        FormSchema formSchema = new FormSchema(tenant, name, 1, schema);
        formSchema.setDescription(description);
        formSchema.setValidationRules(validationRules);
        formSchema.setFieldPermissions(fieldPermissions);
        formSchema.setCreatedBy(createdBy);
        formSchema.setIsActive(true);

        FormSchema savedForm = formSchemaRepository.save(formSchema);

        // Audit log
        auditService.logAction("FORM_CREATED", "FormSchema", savedForm.getId().toString(),
                Map.of("name", name, "version", 1));

        return savedForm;
    }

    /**
     * Update form schema - creates new version (T082)
     */
    public FormSchema updateFormSchema(UUID formId, String schema, String updatedBy) {
        FormSchema existingForm = formSchemaRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form schema not found: " + formId));

        // Validate schema size
        validateSchemaSize(schema);

        // Validate schema JSON structure
        validateSchemaJson(schema);

        // Get next version number
        int nextVersion = existingForm.getVersion() + 1;

        // Create new version
        FormSchema newVersion = new FormSchema();
        newVersion.setTenant(existingForm.getTenant());
        newVersion.setName(existingForm.getName());
        newVersion.setDescription(existingForm.getDescription());
        newVersion.setVersion(nextVersion);
        newVersion.setSchema(schema);
        newVersion.setValidationRules(existingForm.getValidationRules());
        newVersion.setFieldPermissions(existingForm.getFieldPermissions());
        newVersion.setCreatedBy(updatedBy);
        newVersion.setIsActive(true);
        newVersion.setProcessDefinitionKey(existingForm.getProcessDefinitionKey());
        newVersion.setTaskDefinitionKey(existingForm.getTaskDefinitionKey());

        // Deactivate old version
        existingForm.setIsActive(false);
        formSchemaRepository.save(existingForm);

        FormSchema savedForm = formSchemaRepository.save(newVersion);

        // Audit log
        auditService.logAction("FORM_VERSION_CREATED", "FormSchema", savedForm.getId().toString(),
                Map.of("name", existingForm.getName(), "version", nextVersion, "previousVersion", existingForm.getVersion()));

        return savedForm;
    }

    /**
     * Get form schema by ID
     */
    public FormSchema getFormSchema(UUID formId) {
        return formSchemaRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form schema not found: " + formId));
    }

    /**
     * Get form schema for process instance (T083)
     */
    public FormSchema getFormForProcessInstance(UUID processInstanceId, String tenantCode) {
        // In a real implementation, you would fetch the form version from process instance variables
        // For now, return the latest active form
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantCode));

        List<FormSchema> activeForms = formSchemaRepository.findActiveByTenantId(tenant.getId());
        if (activeForms.isEmpty()) {
            throw new RuntimeException("No active forms found for tenant: " + tenantCode);
        }

        return activeForms.get(0); // Return first active form
    }

    /**
     * Get form schema by version
     */
    public FormSchema getFormSchemaByVersion(UUID formId, int version) {
        return formSchemaRepository.findByIdAndVersion(formId, version)
                .orElseThrow(() -> new RuntimeException("Form schema version not found: " + formId + " v" + version));
    }

    /**
     * Get all form versions
     */
    public List<FormSchema> getFormVersions(UUID formId) {
        FormSchema form = formSchemaRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form schema not found: " + formId));

        return formSchemaRepository.findByTenantIdAndName(form.getTenant().getId(), form.getName());
    }

    /**
     * Get all active forms for tenant
     */
    public List<FormSchema> getActiveForms(String tenantCode) {
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantCode));

        return formSchemaRepository.findActiveByTenantId(tenant.getId());
    }

    /**
     * Validate form data against schema (T084)
     */
    public Map<String, Object> validateFormData(UUID formId, Map<String, Object> formData) {
        FormSchema formSchema = getFormSchema(formId);

        Map<String, Object> validationResult = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean isValid = true;

        try {
            // Parse schema JSON
            JsonNode schemaNode = objectMapper.readTree(formSchema.getSchema());

            // Check required fields
            if (schemaNode.has("fields")) {
                JsonNode fieldsNode = schemaNode.get("fields");
                Iterator<JsonNode> fields = fieldsNode.elements();

                while (fields.hasNext()) {
                    JsonNode field = fields.next();
                    String fieldName = field.has("name") ? field.get("name").asText() : null;
                    boolean isRequired = field.has("isRequired") ? field.get("isRequired").asBoolean() : false;
                    String fieldType = field.has("type") ? field.get("type").asText() : "text";

                    if (fieldName != null && isRequired && !formData.containsKey(fieldName)) {
                        errors.add("Required field missing: " + fieldName);
                        isValid = false;
                    }

                    // Type validation
                    if (formData.containsKey(fieldName)) {
                        Object value = formData.get(fieldName);
                        if (!validateFieldType(value, fieldType)) {
                            errors.add("Invalid type for field " + fieldName + ": expected " + fieldType);
                            isValid = false;
                        }
                    }
                }
            }

            // Check field-level permissions (T085)
            Map<String, Object> permissionWarningsMap = new HashMap<>();
            if (formSchema.getFieldPermissions() != null) {
                permissionWarningsMap = validateFieldPermissions(
                        formSchema.getFieldPermissions(), formData);
            }

            if (!permissionWarningsMap.isEmpty()) {
                warnings.addAll(permissionWarningsMap.values().stream()
                        .map(obj -> obj.toString())
                        .collect(Collectors.toList()));
            }

            validationResult.put("valid", isValid);
            validationResult.put("errors", errors);
            validationResult.put("warnings", warnings);
            validationResult.put("permissionWarnings", permissionWarningsMap);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse form schema", e);
        }

        return validationResult;
    }

    /**
     * Deactivate form schema
     */
    public void deactivateFormSchema(UUID formId) {
        FormSchema formSchema = formSchemaRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form schema not found: " + formId));

        formSchema.setIsActive(false);
        formSchemaRepository.save(formSchema);

        // Audit log
        auditService.logAction("FORM_DEACTIVATED", "FormSchema", formId.toString(),
                Map.of("name", formSchema.getName()));
    }

    /**
     * Map form data to process variables (T083)
     */
    public Map<String, Object> mapFormToProcessVariables(UUID formId, Map<String, Object> formData) {
        FormSchema formSchema = getFormSchema(formId);

        // Validate form data first
        Map<String, Object> validationResult = validateFormData(formId, formData);
        if (!(Boolean) validationResult.get("valid")) {
            throw new RuntimeException("Form validation failed: " + validationResult.get("errors"));
        }

        // Create process variables map
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("formData", formData); // Store form data as JSON
        processVariables.put("formId", formId.toString());
        processVariables.put("formVersion", formSchema.getVersion());
        processVariables.put("formName", formSchema.getName());

        // Add individual form fields as process variables
        processVariables.putAll(formData);

        return processVariables;
    }

    /**
     * Get forms for specific process definition
     */
    public List<FormSchema> getFormsForProcessDefinition(String tenantCode, String processDefinitionKey) {
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantCode));

        return formSchemaRepository.findByTenantIdAndProcessDefinitionKey(tenant.getId(), processDefinitionKey);
    }

    /**
     * Get forms for specific task definition
     */
    public List<FormSchema> getFormsForTaskDefinition(String tenantCode, String taskDefinitionKey) {
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantCode));

        return formSchemaRepository.findByTenantIdAndTaskDefinitionKey(tenant.getId(), taskDefinitionKey);
    }

    // Private helper methods

    private void validateSchemaSize(String schema) {
        if (schema != null && schema.length() > 10000) {
            throw new IllegalArgumentException("Form schema exceeds maximum size of 10KB");
        }
    }

    private void validateSchemaJson(String schema) {
        try {
            objectMapper.readTree(schema);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid form schema JSON: " + e.getMessage(), e);
        }
    }

    private boolean validateFieldType(Object value, String fieldType) {
        if (value == null) {
            return true;
        }

        try {
            switch (fieldType.toLowerCase()) {
                case "text":
                case "textarea":
                    return value instanceof String;
                case "number":
                    return value instanceof Number || (value instanceof String && ((String) value).matches("-?\\d+(\\.\\d+)?"));
                case "date":
                    return value instanceof String && isValidDateString((String) value);
                case "checkbox":
                    return value instanceof Boolean || value instanceof String;
                case "select":
                case "radio":
                    return value instanceof String;
                case "file":
                    return value instanceof String || value instanceof Map; // Could be file path or file object
                default:
                    return true; // Unknown type, accept as valid
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidDateString(String dateString) {
        // Simple date validation - in production, use proper date parsing
        return dateString.matches("\\d{4}-\\d{2}-\\d{2}") ||
               dateString.matches("\\d{2}/\\d{2}/\\d{4}") ||
               dateString.matches("\\d{2}-\\d{2}-\\d{4}");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> validateFieldPermissions(String fieldPermissionsJson, Map<String, Object> formData) {
        Map<String, Object> permissionWarnings = new HashMap<>();

        try {
            JsonNode permissionsNode = objectMapper.readTree(fieldPermissionsJson);
            Iterator<String> fieldNames = permissionsNode.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode permissionNode = permissionsNode.get(fieldName);

                if (permissionNode.has("permission")) {
                    String permission = permissionNode.get("permission").asText();

                    if ("read-only".equals(permission) && formData.containsKey(fieldName)) {
                        permissionWarnings.put(fieldName, "Field " + fieldName + " is read-only");
                    } else if ("hidden".equals(permission) && formData.containsKey(fieldName)) {
                        permissionWarnings.put(fieldName, "Field " + fieldName + " should be hidden");
                    }
                }
            }
        } catch (JsonProcessingException e) {
            // Invalid permissions JSON, skip validation
        }

        return permissionWarnings;
    }
}
