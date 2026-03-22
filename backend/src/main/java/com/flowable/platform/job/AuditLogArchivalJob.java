package com.flowable.platform.job;

import com.flowable.platform.service.ProcessArchivalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditLogArchivalJob {

    private static final Logger log = LoggerFactory.getLogger(AuditLogArchivalJob.class);

    private final ProcessArchivalService processArchivalService;

    public AuditLogArchivalJob(ProcessArchivalService processArchivalService) {
        this.processArchivalService = processArchivalService;
    }

    @Scheduled(cron = "${app.archival.cron:0 0 2 * * ?}")
    public void archiveOldProcessInstances() {
        log.info("Starting scheduled process archival job");
        int archived = processArchivalService.archiveCompletedProcesses(365);
        log.info("Archival job completed: {} instances archived", archived);
    }
}
