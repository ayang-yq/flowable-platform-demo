package com.flowable.platform.repository;

import com.flowable.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndUsername(UUID tenantId, String username);

    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.username = :username AND u.isActive = true")
    Optional<User> findActiveByTenantIdAndUsername(@Param("tenantId") UUID tenantId, @Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.tenant.id = :tenantId AND u.username = :username AND u.isActive = true")
    Optional<User> findActiveByTenantIdAndUsernameWithRoles(@Param("tenantId") UUID tenantId, @Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.email = :email AND u.isActive = true")
    Optional<User> findActiveByTenantIdAndEmail(@Param("tenantId") UUID tenantId, @Param("email") String email);

    List<User> findByTenantIdAndIsActiveTrue(UUID tenantId);

    boolean existsByTenantIdAndUsername(UUID tenantId, String username);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}
