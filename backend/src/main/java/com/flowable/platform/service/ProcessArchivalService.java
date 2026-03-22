package com.flowable.platform.service;

import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ProcessArchivalService {

    private static final Logger log = LoggerFactory.getLogger(ProcessArchivalService.class);

    private final HistoryService historyService;

    public ProcessArchivalService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public int archiveCompletedProcesses(int daysOld) {
        Date cutoff = Date.from(LocalDateTime.now().minusDays(daysOld)
                .atZone(ZoneId.systemDefault()).toInstant());

        List<HistoricProcessInstance> oldInstances = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .finishedBefore(cutoff)
                .listPage(0, 1000);

        int archived = 0;
        for (HistoricProcessInstance instance : oldInstances) {
            try {
                historyService.deleteHistoricProcessInstance(instance.getId());
                archived++;
            } catch (Exception e) {
                log.warn("Failed to archive process instance {}: {}", instance.getId(), e.getMessage());
            }
        }

        log.info("Archived {} process instances older than {} days", archived, daysOld);
        return archived;
    }
}
