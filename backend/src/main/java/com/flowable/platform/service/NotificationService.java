package com.flowable.platform.service;

import com.flowable.platform.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendMentionNotification(User mentionedUser, User author, String content,
                                        String taskId, String processInstanceId) {
        String context = taskId != null ? "task " + taskId : "process " + processInstanceId;
        log.info("Sending @mention notification to {} from {} in {}",
                mentionedUser.getUsername(), author.getUsername(), context);
        // In production: send email via JavaMail
        // mailService.send(mentionedUser.getEmail(), "You were mentioned", content);
    }

    @Async
    public void sendTaskAssignmentNotification(User assignee, String taskName, String taskId) {
        log.info("Sending task assignment notification to {} for task {}",
                assignee.getUsername(), taskName);
    }

    @Async
    public void sendTaskExpirationNotification(User assignee, String taskName, String taskId) {
        log.info("Sending task expiration notification to {} for task {}",
                assignee.getUsername(), taskName);
    }
}
