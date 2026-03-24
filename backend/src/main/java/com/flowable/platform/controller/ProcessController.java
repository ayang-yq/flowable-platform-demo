package com.flowable.platform.controller;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.ProcessDTO;
import com.flowable.platform.dto.StartProcessRequest;
import com.flowable.platform.dto.TaskDTO;
import com.flowable.platform.service.ProcessDiagramService;
import com.flowable.platform.service.ProcessService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final ProcessService processService;
    private final ProcessDiagramService processDiagramService;
    private final RepositoryService repositoryService;
    private final CmmnTaskService cmmnTaskService;

    public ProcessController(ProcessService processService,
                             ProcessDiagramService processDiagramService,
                             RepositoryService repositoryService,
                             CmmnTaskService cmmnTaskService) {
        this.processService = processService;
        this.processDiagramService = processDiagramService;
        this.repositoryService = repositoryService;
        this.cmmnTaskService = cmmnTaskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProcessDTO>> startProcess(@RequestBody StartProcessRequest request) {
        ProcessInstance instance = processService.startProcessInstance(
                request.getProcessDefinitionKey(),
                request.getVariables()
        );

        ProcessDTO dto = processService.getProcessInstanceDetails(instance.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcessDTO>>> listProcessInstances() {
        List<ProcessDTO> instances = processService.listProcessInstances();
        return ResponseEntity.ok(ApiResponse.success(instances));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessDTO>> getProcessInstance(@PathVariable String id) {
        ProcessDTO dto = processService.getProcessInstanceDetails(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendProcessInstance(@PathVariable String id) {
        processService.suspendProcessInstance(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateProcessInstance(@PathVariable String id) {
        processService.activateProcessInstance(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/terminate")
    public ResponseEntity<ApiResponse<Void>> terminateProcessInstance(@PathVariable String id) {
        processService.terminateProcessInstance(id, "Terminated by user");
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getProcessTasks(@PathVariable String id) {
        List<TaskDTO> tasks = processService.getTasksForProcessInstance(id);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PostMapping("/definitions")
    public ResponseEntity<ApiResponse<Map<String, String>>> deployProcessDefinition(
            @RequestParam("file") MultipartFile file) throws IOException {

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", "File is empty"));
        }

        // Get file extension to determine type
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", "Invalid file name"));
        }

        // Get tenant context
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Deploy based on file type
        Deployment deployment;
        if (filename.endsWith(".bpmn") || filename.endsWith(".bpmn20.xml")) {
            deployment = repositoryService.createDeployment()
                    .name(filename)
                    .addInputStream(filename, file.getInputStream())
                    .tenantId(tenantId)
                    .deploy();
        } else if (filename.endsWith(".cmmn") || filename.endsWith(".cmmn.xml")) {
            // For CMMN files, we'd need CmmnRepositoryService
            // This is a placeholder for CMMN deployment
            return ResponseEntity.status(501)
                    .body(ApiResponse.error("NOT_IMPLEMENTED", "CMMN deployment not yet implemented"));
        } else if (filename.endsWith(".dmn") || filename.endsWith(".dmn.xml")) {
            // For DMN files, we'd need DmnRepositoryService
            return ResponseEntity.status(501)
                    .body(ApiResponse.error("NOT_IMPLEMENTED", "DMN deployment not yet implemented"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", "Unsupported file type. Expected .bpmn, .cmmn, or .dmn"));
        }

        Map<String, String> response = new HashMap<>();
        response.put("deploymentId", deployment.getId());
        response.put("message", "Process definition deployed successfully");

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listProcessDefinitions() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Get both tenant-specific AND global (empty tenant) process definitions
        // Global processes are auto-deployed from classpath and available to all tenants
        List<org.flowable.engine.repository.ProcessDefinition> tenantDefinitions =
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionTenantId(tenantId)
                        .latestVersion()
                        .list();

        List<org.flowable.engine.repository.ProcessDefinition> globalDefinitions =
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionTenantId("")  // Empty string = global processes
                        .latestVersion()
                        .list();

        // Merge both lists, removing duplicates (in case a process exists in both)
        List<org.flowable.engine.repository.ProcessDefinition> allDefinitions = new java.util.ArrayList<>(tenantDefinitions);
        for (org.flowable.engine.repository.ProcessDefinition globalDef : globalDefinitions) {
            // Only add global definition if not already present in tenant-specific list
            boolean exists = tenantDefinitions.stream()
                    .anyMatch(def -> def.getKey().equals(globalDef.getKey()));
            if (!exists) {
                allDefinitions.add(globalDef);
            }
        }

        List<Map<String, Object>> result = allDefinitions.stream()
                .map(def -> {
                    Map<String, Object> defMap = new HashMap<>();
                    defMap.put("id", def.getId());
                    defMap.put("key", def.getKey());
                    defMap.put("name", def.getName());
                    defMap.put("version", def.getVersion());
                    defMap.put("deploymentId", def.getDeploymentId());
                    defMap.put("tenantId", def.getTenantId());
                    return defMap;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}/diagram")
    public ResponseEntity<ApiResponse<String>> getProcessDiagram(@PathVariable String id) {
        String svg = processDiagramService.getProcessDiagramSvg(id);
        return ResponseEntity.ok(ApiResponse.success(svg));
    }

    /**
     * T045a: Create ad-hoc task within a CMMN case instance
     * POST /api/processes/cases/{caseInstanceId}/ad-hoc-tasks
     */
    @PostMapping("/cases/{caseInstanceId}/ad-hoc-tasks")
    public ResponseEntity<ApiResponse<Map<String, String>>> createAdHocTask(
            @PathVariable String caseInstanceId,
            @RequestBody Map<String, Object> requestBody) {

        // Validate request body
        String taskName = (String) requestBody.get("taskName");
        String description = (String) requestBody.get("description");
        String assignee = (String) requestBody.get("assignee");

        if (taskName == null || taskName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", "Task name is required"));
        }

        // Verify case instance exists by checking for any tasks in the case
        List<Task> caseTasks = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstanceId)
                .list();

        if (caseTasks.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NOT_FOUND", "Case instance not found: " + caseInstanceId));
        }

        // Get tenant context
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Create ad-hoc task (simplified for now)
        org.flowable.task.api.Task adHocTask = cmmnTaskService.newTask();
        adHocTask.setName(taskName);
        adHocTask.setTenantId(tenantId);

        if (description != null && !description.trim().isEmpty()) {
            adHocTask.setDescription(description);
        }

        cmmnTaskService.saveTask(adHocTask);

        // Assign task if assignee provided
        if (assignee != null && !assignee.trim().isEmpty()) {
            cmmnTaskService.setAssignee(adHocTask.getId(), assignee);
        }

        Map<String, String> response = new HashMap<>();
        response.put("taskId", adHocTask.getId());
        response.put("taskName", adHocTask.getName());
        response.put("caseInstanceId", caseInstanceId);
        response.put("message", "Ad-hoc task created successfully");

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
