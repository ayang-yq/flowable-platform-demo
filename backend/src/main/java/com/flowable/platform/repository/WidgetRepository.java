package com.flowable.platform.repository;

import com.flowable.platform.entity.Widget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WidgetRepository extends JpaRepository<Widget, UUID> {

    List<Widget> findByDashboardIdOrderByPositionAsc(UUID dashboardId);
}
