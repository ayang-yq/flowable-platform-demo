package com.flowable.platform.repository;

import com.flowable.platform.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTaskIdOrderByCreatedAtDesc(String taskId);
    List<Comment> findByProcessInstanceIdOrderByCreatedAtDesc(String processInstanceId);
    List<Comment> findByTenantIdAndTaskId(UUID tenantId, String taskId);
    List<Comment> findByTenantIdAndProcessInstanceId(UUID tenantId, String processInstanceId);
}
