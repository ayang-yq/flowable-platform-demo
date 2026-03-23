package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.*;
import org.flowable.dmn.api.DmnDecision;
import org.flowable.dmn.api.DmnDeployment;
import org.flowable.dmn.api.DmnDecisionService;
import org.flowable.dmn.api.DmnRepositoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DecisionService {

    private final DmnRepositoryService dmnRepositoryService;
    private final DmnDecisionService dmnRuleService;
    private final AuditService auditService;

    public DecisionService(DmnRepositoryService dmnRepositoryService,
                           DmnDecisionService dmnRuleService,
                           AuditService auditService) {
        this.dmnRepositoryService = dmnRepositoryService;
        this.dmnRuleService = dmnRuleService;
        this.auditService = auditService;
    }

    public List<DefinitionDTO> listDecisionDefinitions() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        var query = dmnRepositoryService.createDecisionQuery().latestVersion();
        if (tenantId != null) {
            query.decisionTenantId(tenantId);
        }

        List<DmnDecision> decisions = query.list();

        return decisions.stream()
                .map(this::toDefinitionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DecisionExecutionDTO executeDecision(String decisionKey, Map<String, Object> inputVariables) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Verify definition exists
        var defQuery = dmnRepositoryService.createDecisionQuery()
                .decisionKey(decisionKey)
                .latestVersion();
        if (tenantId != null) {
            defQuery.decisionTenantId(tenantId);
        }
        DmnDecision definition = defQuery.singleResult();
        if (definition == null) {
            throw new IllegalArgumentException("Decision definition not found: " + decisionKey);
        }

        // Execute decision
        Map<String, Object> inputs = inputVariables != null ? inputVariables : Collections.emptyMap();
        List<Map<String, Object>> results;

        if (tenantId != null) {
            results = dmnRuleService.createExecuteDecisionBuilder()
                    .decisionKey(decisionKey)
                    .variables(inputs)
                    .tenantId(tenantId)
                    .execute();
        } else {
            results = dmnRuleService.createExecuteDecisionBuilder()
                    .decisionKey(decisionKey)
                    .variables(inputs)
                    .execute();
        }

        auditService.logAction("DECISION_EXECUTED", "DECISION", definition.getId(), null);

        DecisionExecutionDTO dto = new DecisionExecutionDTO();
        dto.setDecisionKey(decisionKey);
        dto.setDecisionName(definition.getName());
        dto.setExecutionId(UUID.randomUUID().toString());
        dto.setInputVariables(inputs);
        dto.setOutputVariables(results);
        dto.setExecutionTime(LocalDateTime.now());

        return dto;
    }

    public List<InstanceDTO> getDecisionExecutionHistory(int page, int size) {
        // DMN executions don't have persistent history in the same way as BPMN/CMMN
        // unless explicitly configured. Return empty for now.
        return Collections.emptyList();
    }

    public long countDecisionExecutions() {
        // DMN executions are tracked in history if enabled
        return 0;
    }

    private DefinitionDTO toDefinitionDTO(DmnDecision decision) {
        LocalDateTime deploymentTime = null;
        if (decision.getDeploymentId() != null) {
            DmnDeployment deployment = dmnRepositoryService.createDeploymentQuery()
                    .deploymentId(decision.getDeploymentId())
                    .singleResult();
            if (deployment != null && deployment.getDeploymentTime() != null) {
                deploymentTime = deployment.getDeploymentTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        }

        return new DefinitionDTO(
                decision.getId(), decision.getKey(), decision.getName(), decision.getVersion(),
                decision.getCategory(), DefinitionType.DMN, false, deploymentTime
        );
    }
}
