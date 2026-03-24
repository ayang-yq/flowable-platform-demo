package com.flowable.platform.controller;

import com.flowable.platform.dto.*;
import com.flowable.platform.service.CaseService;
import com.flowable.platform.service.DecisionService;
import com.flowable.platform.service.ProcessService;
import com.flowable.platform.service.WorkspaceService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final ProcessService processService;
    private final CaseService caseService;
    private final DecisionService decisionService;

    public WorkspaceController(WorkspaceService workspaceService,
                               ProcessService processService,
                               CaseService caseService,
                               DecisionService decisionService) {
        this.workspaceService = workspaceService;
        this.processService = processService;
        this.caseService = caseService;
        this.decisionService = decisionService;
    }

    @GetMapping("/definitions")
    public ResponseEntity<ApiResponse<List<DefinitionDTO>>> listDefinitions(
            @RequestParam(defaultValue = "all") String type) {
        List<DefinitionDTO> definitions = workspaceService.getAllDefinitions(type);
        return ResponseEntity.ok(ApiResponse.success(definitions));
    }

    @PostMapping("/processes/{definitionKey}/start")
    public ResponseEntity<ApiResponse<InstanceDTO>> startProcess(
            @PathVariable String definitionKey,
            @RequestBody StartInstanceRequest request) {
        ProcessInstance instance = processService.startProcessInstance(
                definitionKey,
                request.getVariables()
        );

        InstanceDTO dto = new InstanceDTO();
        dto.setId(instance.getId());
        dto.setDefinitionId(instance.getProcessDefinitionId());
        dto.setDefinitionKey(instance.getProcessDefinitionKey());
        dto.setDefinitionName(instance.getProcessDefinitionName());
        dto.setType(DefinitionType.BPMN);
        dto.setStartTime(LocalDateTime.now());
        dto.setStartedBy(instance.getStartUserId());
        dto.setStatus(InstanceStatus.ACTIVE);
        dto.setBusinessKey(request.getBusinessKey());
        dto.setTenantId(instance.getTenantId());

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/cases/{definitionKey}/start")
    public ResponseEntity<ApiResponse<InstanceDTO>> startCase(
            @PathVariable String definitionKey,
            @RequestBody StartInstanceRequest request) {
        InstanceDTO dto = caseService.startCaseInstance(
                definitionKey,
                request.getVariables(),
                request.getBusinessKey()
        );
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/decisions/{definitionKey}/execute")
    public ResponseEntity<ApiResponse<DecisionExecutionDTO>> executeDecision(
            @PathVariable String definitionKey,
            @RequestBody ExecuteDecisionRequest request) {
        DecisionExecutionDTO result = decisionService.executeDecision(
                definitionKey,
                request.getInputVariables()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/instances/active")
    public ResponseEntity<ApiResponse<InstancePageDTO>> getActiveInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startedBy,
            @RequestParam(required = false) LocalDateTime startDateFrom,
            @RequestParam(required = false) LocalDateTime startDateTo) {
        InstancePageDTO result = workspaceService.getActiveInstances(
                page, size, type, search, startedBy, startDateFrom, startDateTo);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/instances/completed")
    public ResponseEntity<ApiResponse<InstancePageDTO>> getCompletedInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startedBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDateFrom,
            @RequestParam(required = false) LocalDateTime startDateTo,
            @RequestParam(required = false) LocalDateTime endDateFrom,
            @RequestParam(required = false) LocalDateTime endDateTo) {
        InstanceStatus statusFilter = null;
        if (status != null && !"all".equalsIgnoreCase(status)) {
            statusFilter = InstanceStatus.valueOf(status.toUpperCase());
        }
        InstancePageDTO result = workspaceService.getCompletedInstances(
                page, size, type, search, startedBy, statusFilter,
                startDateFrom, startDateTo, endDateFrom, endDateTo);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/instances/{id}")
    public ResponseEntity<ApiResponse<InstanceDetailDTO>> getInstanceDetail(
            @PathVariable String id,
            @RequestParam String type) {
        InstanceDetailDTO detail = workspaceService.getInstanceDetail(id, type);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        DashboardSummaryDTO summary = workspaceService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/process-instances/{id}/diagram")
    public ResponseEntity<ApiResponse<DiagramDataDTO>> getProcessInstanceDiagram(@PathVariable String id) {
        DiagramDataDTO diagramData = processService.getProcessInstanceDiagram(id);
        if (diagramData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(diagramData));
    }

    @GetMapping("/case-instances/{id}/diagram")
    public ResponseEntity<ApiResponse<DiagramDataDTO>> getCaseInstanceDiagram(@PathVariable String id) {
        DiagramDataDTO diagramData = caseService.getCaseInstanceDiagram(id);
        if (diagramData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(diagramData));
    }
}
