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

        // Get both tenant-specific AND global (null tenant) decision definitions
        // Global definitions are auto-deployed from classpath and available to all tenants
        List<DmnDecision> allDecisions = new ArrayList<>();

        // Add tenant-specific decisions
        if (tenantId != null) {
            List<DmnDecision> tenantDecisions = dmnRepositoryService
                    .createDecisionQuery()
                    .latestVersion()
                    .decisionTenantId(tenantId)
                    .list();
            allDecisions.addAll(tenantDecisions);
        }

        // Add global (auto-deployed) decisions
        List<DmnDecision> globalDecisions = dmnRepositoryService
                .createDecisionQuery()
                .latestVersion()
                .decisionTenantId("")  // Empty string = global decisions
                .list();

        // Merge, avoiding duplicates by key
        for (DmnDecision globalDef : globalDecisions) {
            boolean exists = allDecisions.stream()
                    .anyMatch(def -> def.getKey().equals(globalDef.getKey()));
            if (!exists) {
                allDecisions.add(globalDef);
            }
        }

        return allDecisions.stream()
                .map(this::toDefinitionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DecisionExecutionDTO executeDecision(String decisionKey, Map<String, Object> inputVariables) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Find decision definition (check tenant-specific first, then global)
        DmnDecision definition = null;

        if (tenantId != null) {
            // Try tenant-specific first
            definition = dmnRepositoryService.createDecisionQuery()
                    .decisionKey(decisionKey)
                    .decisionTenantId(tenantId)
                    .latestVersion()
                    .singleResult();
        }

        // Fall back to global (empty tenant) if not found
        if (definition == null) {
            definition = dmnRepositoryService.createDecisionQuery()
                    .decisionKey(decisionKey)
                    .decisionTenantId("")
                    .latestVersion()
                    .singleResult();
        }

        if (definition == null) {
            throw new IllegalArgumentException("Decision definition not found: " + decisionKey);
        }

        // Execute decision
        Map<String, Object> inputs = inputVariables != null ? inputVariables : Collections.emptyMap();
        List<Map<String, Object>> results;

        if (tenantId != null && !"".equals(definition.getTenantId())) {
            // Tenant-specific decision
            results = dmnRuleService.createExecuteDecisionBuilder()
                    .decisionKey(decisionKey)
                    .variables(inputs)
                    .tenantId(tenantId)
                    .execute();
        } else {
            // Global decision
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
