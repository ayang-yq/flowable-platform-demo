package com.flowable.platform.repository;

import com.flowable.platform.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByTaskIdOrderByCreatedAtDesc(String taskId);
    List<Attachment> findByProcessInstanceIdOrderByCreatedAtDesc(String processInstanceId);
    List<Attachment> findByTenantIdAndTaskId(UUID tenantId, String taskId);
    List<Attachment> findByTenantIdAndProcessInstanceId(UUID tenantId, String processInstanceId);
}
