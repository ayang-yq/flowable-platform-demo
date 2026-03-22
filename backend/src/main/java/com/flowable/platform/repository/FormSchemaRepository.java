package com.flowable.platform.repository;

import com.flowable.platform.entity.FormSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormSchemaRepository extends JpaRepository<FormSchema, UUID> {

    List<FormSchema> findByTenantId(UUID tenantId);

    @Query("SELECT f FROM FormSchema f WHERE f.tenant.id = :tenantId AND f.name = :name ORDER BY f.version DESC")
    List<FormSchema> findByTenantIdAndName(@Param("tenantId") UUID tenantId, @Param("name") String name);

    @Query("SELECT f FROM FormSchema f WHERE f.tenant.id = :tenantId AND f.name = :name AND f.version = :version")
    Optional<FormSchema> findByTenantIdAndNameAndVersion(@Param("tenantId") UUID tenantId, @Param("name") String name, @Param("version") int version);

    @Query("SELECT f FROM FormSchema f WHERE f.tenant.id = :tenantId AND f.isActive = true ORDER BY f.createdAt DESC")
    List<FormSchema> findActiveByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT f FROM FormSchema f WHERE f.tenant.id = :tenantId AND f.processDefinitionKey = :processKey ORDER BY f.createdAt DESC")
    List<FormSchema> findByTenantIdAndProcessDefinitionKey(@Param("tenantId") UUID tenantId, @Param("processKey") String processKey);

    @Query("SELECT f FROM FormSchema f WHERE f.tenant.id = :tenantId AND f.taskDefinitionKey = :taskKey ORDER BY f.createdAt DESC")
    List<FormSchema> findByTenantIdAndTaskDefinitionKey(@Param("tenantId") UUID tenantId, @Param("taskKey") String taskKey);

    @Query("SELECT MAX(f.version) FROM FormSchema f WHERE f.tenant.id = :tenantId AND f.name = :name")
    Optional<Integer> findLatestVersionByTenantIdAndName(@Param("tenantId") UUID tenantId, @Param("name") String name);

    boolean existsByTenantIdAndNameAndVersion(UUID tenantId, String name, int version);

    @Query("SELECT f FROM FormSchema f WHERE f.tenant.id = :tenantId AND f.id = :formId AND f.version = :version")
    Optional<FormSchema> findByIdAndVersion(@Param("formId") UUID formId, @Param("version") int version);
}
