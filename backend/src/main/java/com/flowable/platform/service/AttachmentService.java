package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.entity.Attachment;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.AttachmentRepository;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AttachmentService(AttachmentRepository attachmentRepository, TenantRepository tenantRepository,
                            UserRepository userRepository, AuditService auditService) {
        this.attachmentRepository = attachmentRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    private UUID getTenantId() {
        String tenantIdStr = MultiTenantFilter.getCurrentTenantId();
        if (tenantIdStr == null) throw new IllegalStateException("No tenant context available");
        return UUID.fromString(tenantIdStr);
    }

    public List<Attachment> getTaskAttachments(String taskId) {
        return attachmentRepository.findByTenantIdAndTaskId(getTenantId(), taskId);
    }

    public List<Attachment> getProcessAttachments(String processInstanceId) {
        return attachmentRepository.findByTenantIdAndProcessInstanceId(getTenantId(), processInstanceId);
    }

    @Transactional
    public Attachment addTaskAttachment(String taskId, String uploaderUsername,
                                       String fileName, long fileSize, String mimeType) {
        return addAttachment(taskId, null, uploaderUsername, fileName, fileSize, mimeType);
    }

    @Transactional
    public Attachment addProcessAttachment(String processInstanceId, String uploaderUsername,
                                          String fileName, long fileSize, String mimeType) {
        return addAttachment(null, processInstanceId, uploaderUsername, fileName, fileSize, mimeType);
    }

    private Attachment addAttachment(String taskId, String processInstanceId, String uploaderUsername,
                                    String fileName, long fileSize, String mimeType) {
        UUID tenantId = getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        User uploader = userRepository.findByTenantIdAndUsername(tenantId, uploaderUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + uploaderUsername));

        // Build tenant-isolated storage path
        String storagePath = buildStoragePath(tenantId, fileName);

        Attachment attachment = new Attachment();
        attachment.setTenant(tenant);
        attachment.setUploader(uploader);
        attachment.setFileName(fileName);
        attachment.setFileSize(fileSize);
        attachment.setMimeType(mimeType);
        attachment.setStoragePath(storagePath);
        attachment.setStorageProvider("local");
        attachment.setTaskId(taskId);
        attachment.setProcessInstanceId(processInstanceId);

        Attachment saved = attachmentRepository.save(attachment);
        String entityId = taskId != null ? taskId : processInstanceId;
        auditService.logAction("ATTACHMENT_UPLOADED", "ATTACHMENT", entityId, null);
        return saved;
    }

    private String buildStoragePath(UUID tenantId, String fileName) {
        return "/tenant/" + tenantId + "/attachments/" + UUID.randomUUID() + "/" + fileName;
    }
}
