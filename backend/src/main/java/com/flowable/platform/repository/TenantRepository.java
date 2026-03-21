package com.flowable.platform.repository;

import com.flowable.platform.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByCode(String code);

    Optional<Tenant> findByDomain(String domain);

    @Query("SELECT t FROM Tenant t WHERE t.code = :code AND t.isActive = true")
    Optional<Tenant> findActiveByCode(String code);

    boolean existsByCode(String code);

    boolean existsByDomain(String domain);
}
