package com.flowable.platform.repository;

import com.flowable.platform.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByTenantIdAndCode(UUID tenantId, String code);

    List<Department> findByTenantIdAndParentIdIsNull(UUID tenantId);

    List<Department> findByParentId(UUID parentId);

    @Query("SELECT d FROM Department d WHERE d.tenant.id = :tenantId AND d.path LIKE :pathPrefix%")
    List<Department> findAllDescendants(@org.springframework.data.repository.query.Param("tenantId") UUID tenantId,
                                        @org.springframework.data.repository.query.Param("pathPrefix") String pathPrefix);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);
}
