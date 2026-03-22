package com.flowable.platform.service;

import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final HistoryService historyService;

    public AnalyticsService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public Map<String, Object> getTaskCompletionEfficiency() {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage(0, 500);

        Map<String, List<Long>> durationsByProcess = new HashMap<>();
        for (HistoricTaskInstance task : tasks) {
            if (task.getDurationInMillis() != null) {
                durationsByProcess.computeIfAbsent(
                        task.getProcessDefinitionId() != null ? task.getProcessDefinitionId() : "unknown",
                        k -> new ArrayList<>()
                ).add(task.getDurationInMillis());
            }
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Double> avgDurations = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : durationsByProcess.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
            avgDurations.put(entry.getKey(), avg / 1000.0 / 60.0); // Convert to minutes
        }
        result.put("avgCompletionTimeMinutes", avgDurations);
        result.put("totalCompletedTasks", tasks.size());
        return result;
    }

    public Map<String, Object> getProcessDistribution() {
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .listPage(0, 1000);

        Map<String, Long> distribution = instances.stream()
                .collect(Collectors.groupingBy(
                        pi -> pi.getProcessDefinitionKey() != null ? pi.getProcessDefinitionKey() : "unknown",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("distribution", distribution);
        result.put("totalInstances", instances.size());
        return result;
    }

    public Map<String, Object> getBottleneckAnalysis() {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .finished()
                .listPage(0, 1000);

        Map<String, List<Long>> durationsByTask = new HashMap<>();
        for (HistoricTaskInstance task : tasks) {
            if (task.getDurationInMillis() != null && task.getTaskDefinitionKey() != null) {
                durationsByTask.computeIfAbsent(task.getTaskDefinitionKey(), k -> new ArrayList<>())
                        .add(task.getDurationInMillis());
            }
        }

        Map<String, Double> avgDwellTime = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : durationsByTask.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
            avgDwellTime.put(entry.getKey(), avg / 1000.0 / 60.0);
        }

        // Sort by avg dwell time descending to identify bottlenecks
        Map<String, Object> result = new HashMap<>();
        result.put("bottlenecks", avgDwellTime.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));
        return result;
    }

    public Map<String, Object> getSlaComplianceRate() {
        List<HistoricTaskInstance> allTasks = historyService.createHistoricTaskInstanceQuery()
                .finished()
                .listPage(0, 1000);

        long totalWithDueDate = 0;
        long completedOnTime = 0;

        for (HistoricTaskInstance task : allTasks) {
            if (task.getDueDate() != null) {
                totalWithDueDate++;
                if (task.getEndTime() != null && !task.getEndTime().after(task.getDueDate())) {
                    completedOnTime++;
                }
            }
        }

        double complianceRate = totalWithDueDate > 0 ? (double) completedOnTime / totalWithDueDate * 100 : 100.0;

        Map<String, Object> result = new HashMap<>();
        result.put("complianceRate", Math.round(complianceRate * 100.0) / 100.0);
        result.put("totalWithDueDate", totalWithDueDate);
        result.put("completedOnTime", completedOnTime);
        return result;
    }
}
