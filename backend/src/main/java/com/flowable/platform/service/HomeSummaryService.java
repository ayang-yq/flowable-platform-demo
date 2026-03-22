package com.flowable.platform.service;

import com.flowable.platform.dto.ActivityItemDTO;
import com.flowable.platform.dto.HomeSummaryDTO;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class HomeSummaryService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public HomeSummaryService(TaskService taskService, RuntimeService runtimeService, HistoryService historyService) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    public HomeSummaryDTO getSummary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        long pendingTaskCount = taskService.createTaskQuery()
                .taskAssignee(username)
                .count();

        long activeProcessCount = runtimeService.createProcessInstanceQuery()
                .startedBy(username)
                .count();

        List<ActivityItemDTO> recentActivity = getRecentActivity(username);

        return new HomeSummaryDTO(pendingTaskCount, activeProcessCount, recentActivity);
    }

    private List<ActivityItemDTO> getRecentActivity(String username) {
        List<ActivityItemDTO> activities = new ArrayList<>();

        // Recent completed tasks
        List<HistoricTaskInstance> completedTasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(username)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage(0, 5);

        for (HistoricTaskInstance task : completedTasks) {
            LocalDateTime timestamp = task.getEndTime() != null
                    ? task.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now();
            activities.add(new ActivityItemDTO(
                    task.getId(),
                    "TASK_COMPLETED",
                    task.getName() != null ? task.getName() : "Task",
                    timestamp,
                    task.getProcessDefinitionId()
            ));
        }

        // Recent started processes
        List<HistoricProcessInstance> startedProcesses = historyService.createHistoricProcessInstanceQuery()
                .startedBy(username)
                .orderByProcessInstanceStartTime().desc()
                .listPage(0, 5);

        for (HistoricProcessInstance proc : startedProcesses) {
            LocalDateTime timestamp = proc.getStartTime() != null
                    ? proc.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now();
            activities.add(new ActivityItemDTO(
                    proc.getId(),
                    "PROCESS_STARTED",
                    proc.getProcessDefinitionName() != null ? proc.getProcessDefinitionName() : proc.getProcessDefinitionKey(),
                    timestamp,
                    proc.getProcessDefinitionKey()
            ));
        }

        // Sort by timestamp desc and limit to 5
        activities.sort(Comparator.comparing(ActivityItemDTO::getTimestamp).reversed());
        return activities.size() > 5 ? activities.subList(0, 5) : activities;
    }
}
