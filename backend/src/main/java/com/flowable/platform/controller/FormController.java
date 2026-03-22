package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.entity.FormSchema;
import com.flowable.platform.service.FormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for dynamic form schema management.
 * Provides endpoints for CRUD operations, versioning, validation, and process integration.
 */
@RestController
@RequestMapping("/api/forms")
@Tag(name = "Form Management", description = "APIs for managing dynamic form schemas")
@SecurityRequirement(name = "bearerAuth")
public class FormController {

    private final FormService formService;

    @Autowired
    public FormController(FormService formService) {
        this.formService = formService;
    }

    @PostMapping
    @Operation(summary = "Create new form schema", description = "Create a new dynamic form schema with version 1")
    public ResponseEntity<ApiResponse<FormSchema>> createForm(
            @Parameter(description = "Tenant code") @RequestParam String tenantCode,
            @Parameter(description = "Form name") @RequestParam String name,
            @Parameter(description = "Form description") @RequestParam(required = false) String description,
            @Parameter(description = "Form schema JSON") @RequestParam String schema,
            @Parameter(description = "Validation rules JSON") @RequestParam(required = false) String validationRules,
            @Parameter(description = "Field permissions JSON") @RequestParam(required = false) String fieldPermissions) {

        FormSchema formSchema = formService.createFormSchema(
                tenantCode, name, description, schema, validationRules, fieldPermissions,
                "system"); // TODO: Get current user from security context

        return ResponseEntity.ok(ApiResponse.success(formSchema));
    }

    @GetMapping
    @Operation(summary = "Get all forms", description = "Retrieve all form schemas for the current tenant")
    public ResponseEntity<ApiResponse<List<FormSchema>>> getAllForms(
            @Parameter(description = "Tenant code") @RequestParam String tenantCode) {

        List<FormSchema> forms = formService.getActiveForms(tenantCode);
        return ResponseEntity.ok(ApiResponse.success(forms));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active forms", description = "Retrieve only active form schemas")
    public ResponseEntity<ApiResponse<List<FormSchema>>> getActiveForms(
            @Parameter(description = "Tenant code") @RequestParam String tenantCode) {

        List<FormSchema> forms = formService.getActiveForms(tenantCode);
        return ResponseEntity.ok(ApiResponse.success(forms));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get form by ID", description = "Retrieve detailed form schema by ID")
    public ResponseEntity<ApiResponse<FormSchema>> getFormById(
            @Parameter(description = "Form ID") @PathVariable UUID id) {

        FormSchema formSchema = formService.getFormSchema(id);
        return ResponseEntity.ok(ApiResponse.success(formSchema));
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get form version history", description = "Retrieve all versions of a form schema")
    public ResponseEntity<ApiResponse<List<FormSchema>>> getFormVersions(
            @Parameter(description = "Form ID") @PathVariable UUID id) {

        List<FormSchema> versions = formService.getFormVersions(id);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    @GetMapping("/{id}/version/{version}")
    @Operation(summary = "Get specific form version", description = "Retrieve a specific version of a form schema")
    public ResponseEntity<ApiResponse<FormSchema>> getFormByVersion(
            @Parameter(description = "Form ID") @PathVariable UUID id,
            @Parameter(description = "Version number") @PathVariable int version) {

        FormSchema formSchema = formService.getFormSchemaByVersion(id, version);
        return ResponseEntity.ok(ApiResponse.success(formSchema));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update form schema", description = "Update form schema - creates new version automatically")
    public ResponseEntity<ApiResponse<FormSchema>> updateForm(
            @Parameter(description = "Form ID") @PathVariable UUID id,
            @Parameter(description = "Updated form schema JSON") @RequestParam String schema) {

        FormSchema updatedForm = formService.updateFormSchema(id, schema, "system"); // TODO: Get current user
        return ResponseEntity.ok(ApiResponse.success(updatedForm));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate form", description = "Deactivate a form schema (soft delete)")
    public ResponseEntity<ApiResponse<Map<String, String>>> deactivateForm(
            @Parameter(description = "Form ID") @PathVariable UUID id) {

        formService.deactivateFormSchema(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Form deactivated successfully")));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate form data", description = "Validate form data against schema with server-side validation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateFormData(
            @Parameter(description = "Form ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> formData) {

        Map<String, Object> validationResult = formService.validateFormData(id, formData);
        return ResponseEntity.ok(ApiResponse.success(validationResult));
    }

    @GetMapping("/process-instance/{processInstanceId}")
    @Operation(summary = "Get form for process instance", description = "Retrieve form schema associated with a process instance")
    public ResponseEntity<ApiResponse<FormSchema>> getFormForProcessInstance(
            @Parameter(description = "Process instance ID") @PathVariable String processInstanceId,
            @Parameter(description = "Tenant code") @RequestParam String tenantCode) {

        UUID processInstanceIdUuid = UUID.fromString(processInstanceId);
        FormSchema formSchema = formService.getFormForProcessInstance(processInstanceIdUuid, tenantCode);
        return ResponseEntity.ok(ApiResponse.success(formSchema));
    }

    @GetMapping("/process-definition/{processDefinitionKey}")
    @Operation(summary = "Get forms for process definition", description = "Retrieve all forms associated with a process definition")
    public ResponseEntity<ApiResponse<List<FormSchema>>> getFormsForProcessDefinition(
            @Parameter(description = "Process definition key") @PathVariable String processDefinitionKey,
            @Parameter(description = "Tenant code") @RequestParam String tenantCode) {

        List<FormSchema> forms = formService.getFormsForProcessDefinition(tenantCode, processDefinitionKey);
        return ResponseEntity.ok(ApiResponse.success(forms));
    }

    @GetMapping("/task-definition/{taskDefinitionKey}")
    @Operation(summary = "Get forms for task definition", description = "Retrieve all forms associated with a task definition")
    public ResponseEntity<ApiResponse<List<FormSchema>>> getFormsForTaskDefinition(
            @Parameter(description = "Task definition key") @PathVariable String taskDefinitionKey,
            @Parameter(description = "Tenant code") @RequestParam String tenantCode) {

        List<FormSchema> forms = formService.getFormsForTaskDefinition(tenantCode, taskDefinitionKey);
        return ResponseEntity.ok(ApiResponse.success(forms));
    }

    @PostMapping("/{id}/map-variables")
    @Operation(summary = "Map form data to process variables", description = "Transform form data into Flowable process variables")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mapFormToProcessVariables(
            @Parameter(description = "Form ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> formData) {

        Map<String, Object> processVariables = formService.mapFormToProcessVariables(id, formData);
        return ResponseEntity.ok(ApiResponse.success(processVariables));
    }

    // Request/Response DTOs could be added here for more complex operations
}
