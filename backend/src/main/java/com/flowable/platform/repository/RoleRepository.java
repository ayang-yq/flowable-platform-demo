package com.flowable.platform.repository;

import com.flowable.platform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByTenantIdAndCode(UUID tenantId, String code);

    @Query("SELECT r FROM Role r WHERE r.tenant.id = :tenantId AND r.code = :code")
    Optional<Role> findByTenantIdAndCodeAny(@Param("tenantId") UUID tenantId, @Param("code") String code);

    List<Role> findByTenantId(UUID tenantId);

    List<Role> findByTenantIdAndIsSystemTrue(UUID tenantId);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);
}
