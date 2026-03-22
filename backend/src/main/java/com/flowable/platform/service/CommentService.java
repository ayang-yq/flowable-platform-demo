package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.entity.Comment;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.CommentRepository;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private final CommentRepository commentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public CommentService(CommentRepository commentRepository, TenantRepository tenantRepository,
                         UserRepository userRepository, NotificationService notificationService,
                         AuditService auditService) {
        this.commentRepository = commentRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    private UUID getTenantId() {
        String tenantIdStr = MultiTenantFilter.getCurrentTenantId();
        if (tenantIdStr == null) throw new IllegalStateException("No tenant context available");
        return UUID.fromString(tenantIdStr);
    }

    public List<Comment> getTaskComments(String taskId) {
        return commentRepository.findByTenantIdAndTaskId(getTenantId(), taskId);
    }

    public List<Comment> getProcessComments(String processInstanceId) {
        return commentRepository.findByTenantIdAndProcessInstanceId(getTenantId(), processInstanceId);
    }

    @Transactional
    public Comment addTaskComment(String taskId, String authorUsername, String content) {
        return addComment(taskId, null, authorUsername, content);
    }

    @Transactional
    public Comment addProcessComment(String processInstanceId, String authorUsername, String content) {
        return addComment(null, processInstanceId, authorUsername, content);
    }

    private Comment addComment(String taskId, String processInstanceId, String authorUsername, String content) {
        UUID tenantId = getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        User author = userRepository.findByTenantIdAndUsername(tenantId, authorUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authorUsername));

        Comment comment = new Comment();
        comment.setTenant(tenant);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setTaskId(taskId);
        comment.setProcessInstanceId(processInstanceId);

        // Parse @mentions
        List<String> mentionedUsernames = parseMentions(content);
        if (!mentionedUsernames.isEmpty()) {
            List<String> mentionedUserIds = new ArrayList<>();
            for (String username : mentionedUsernames) {
                userRepository.findByTenantIdAndUsername(tenantId, username)
                        .ifPresent(user -> {
                            mentionedUserIds.add(user.getId().toString());
                            notificationService.sendMentionNotification(user, author, content, taskId, processInstanceId);
                        });
            }
            comment.setMentions("[" + String.join(",", mentionedUserIds.stream().map(id -> "\"" + id + "\"").toList()) + "]");
        }

        Comment saved = commentRepository.save(comment);
        String entityId = taskId != null ? taskId : processInstanceId;
        auditService.logAction("COMMENT_ADDED", "COMMENT", entityId, null);
        return saved;
    }

    private List<String> parseMentions(String content) {
        List<String> mentions = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }
}
