package com.flowable.platform.repository;

import com.flowable.platform.entity.DataDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataDictionaryRepository extends JpaRepository<DataDictionary, UUID> {

    List<DataDictionary> findByTenantIdAndCategoryAndIsActiveTrueOrderBySortOrderAsc(UUID tenantId, String category);

    List<DataDictionary> findByTenantIdAndIsActiveTrueOrderByCategoryAscSortOrderAsc(UUID tenantId);

    Optional<DataDictionary> findByTenantIdAndCategoryAndCode(UUID tenantId, String category, String code);

    boolean existsByTenantIdAndCategoryAndCode(UUID tenantId, String category, String code);
}
