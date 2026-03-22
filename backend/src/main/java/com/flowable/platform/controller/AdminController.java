package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.ProcessDTO;
import com.flowable.platform.service.AuditService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final AuditService auditService;

    public AdminController(RuntimeService runtimeService, RepositoryService repositoryService,
                          AuditService auditService) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
        this.auditService = auditService;
    }

    @GetMapping("/processes/definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .orderByProcessDefinitionVersion().desc()
                .list();

        List<Map<String, Object>> result = definitions.stream()
                .map(pd -> Map.<String, Object>of(
                        "id", pd.getId(),
                        "key", pd.getKey(),
                        "name", pd.getName() != null ? pd.getName() : pd.getKey(),
                        "version", pd.getVersion(),
                        "deploymentId", pd.getDeploymentId(),
                        "tenantId", pd.getTenantId() != null ? pd.getTenantId() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/processes/definitions/deploy")
    public ResponseEntity<ApiResponse<Map<String, String>>> deployProcessDefinition(
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_FILE", "File name is required"));
        }

        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(fileName, file.getInputStream())
                .name(fileName)
                .deploy();

        auditService.logAction("ADMIN_DEPLOY", "PROCESS_DEFINITION", deployment.getId(), null);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "deploymentId", deployment.getId(),
                "name", deployment.getName()
        )));
    }

    @GetMapping("/instances")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listProcessInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .orderByProcessInstanceId().desc()
                .listPage(page * size, size);

        long total = runtimeService.createProcessInstanceQuery().count();

        List<Map<String, Object>> result = instances.stream()
                .map(pi -> Map.<String, Object>of(
                        "id", pi.getId(),
                        "processDefinitionId", pi.getProcessDefinitionId(),
                        "processDefinitionKey", pi.getProcessDefinitionKey(),
                        "processDefinitionName", pi.getProcessDefinitionName() != null ? pi.getProcessDefinitionName() : pi.getProcessDefinitionKey(),
                        "startTime", pi.getStartTime() != null ? pi.getStartTime().toString() : "",
                        "isSuspended", pi.isSuspended(),
                        "tenantId", pi.getTenantId() != null ? pi.getTenantId() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/instances/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendInstance(@PathVariable String id) {
        runtimeService.suspendProcessInstanceById(id);
        auditService.logAction("ADMIN_SUSPEND", "PROCESS_INSTANCE", id, null);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/instances/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateInstance(@PathVariable String id) {
        runtimeService.activateProcessInstanceById(id);
        auditService.logAction("ADMIN_ACTIVATE", "PROCESS_INSTANCE", id, null);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/instances/{id}/terminate")
    public ResponseEntity<ApiResponse<Void>> terminateInstance(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Admin termination";
        runtimeService.deleteProcessInstance(id, reason);
        auditService.logAction("ADMIN_TERMINATE", "PROCESS_INSTANCE", id, null);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/instances/{id}/variables")
    public ResponseEntity<ApiResponse<Void>> modifyVariables(
            @PathVariable String id, @RequestBody Map<String, Object> variables) {
        runtimeService.setVariables(id, variables);
        auditService.logAction("ADMIN_MODIFY_VARIABLES", "PROCESS_INSTANCE", id, null);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/instances/{id}/jump")
    public ResponseEntity<ApiResponse<Void>> jumpToNode(
            @PathVariable String id, @RequestBody Map<String, String> body) {
        String targetActivityId = body.get("targetActivityId");
        String currentActivityId = body.get("currentActivityId");

        if (targetActivityId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("MISSING_TARGET", "targetActivityId is required"));
        }

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(id)
                .moveActivityIdTo(currentActivityId, targetActivityId)
                .changeState();

        auditService.logAction("ADMIN_NODE_JUMP", "PROCESS_INSTANCE", id, null);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
