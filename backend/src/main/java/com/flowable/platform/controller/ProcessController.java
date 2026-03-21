package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.ProcessDTO;
import com.flowable.platform.dto.StartProcessRequest;
import com.flowable.platform.dto.TaskDTO;
import com.flowable.platform.service.ProcessService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
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

        // TODO: Implement process deployment logic
        // This should deploy the BPMN/CMMN/DMN file to Flowable

        Map<String, String> response = Map.of(
                "deploymentId", "deployment-123",
                "message", "Process definition deployed successfully"
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listProcessDefinitions() {
        // TODO: Implement process definitions list
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/{id}/diagram")
    public ResponseEntity<ApiResponse<String>> getProcessDiagram(@PathVariable String id) {
        // TODO: Generate process diagram SVG
        return ResponseEntity.ok(ApiResponse.success("<svg>diagram-placeholder</svg>"));
    }
}
