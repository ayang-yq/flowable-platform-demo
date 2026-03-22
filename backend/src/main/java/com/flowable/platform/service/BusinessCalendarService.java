package com.flowable.platform.service;

import com.flowable.platform.entity.Tenant;
import com.flowable.platform.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for calculating business days, excluding weekends and tenant-specific holidays.
 * Provides due date calculations and SLA tracking for tasks.
 */
@Service
public class BusinessCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessCalendarService.class);

    private static final Set<DayOfWeek> DEFAULT_WEEKEND_DAYS = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private final TenantRepository tenantRepository;

    // Default holidays (can be overridden per tenant)
    private static final Set<LocalDate> DEFAULT_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),   // New Year's Day
            LocalDate.of(2026, 7, 4),   // Independence Day
            LocalDate.of(2026, 12, 25), // Christmas
            LocalDate.of(2026, 12, 24), // Christmas Eve
            LocalDate.of(2026, 11, 26), // Thanksgiving
            LocalDate.of(2026, 11, 27)  // Day after Thanksgiving
    );

    @Autowired
    public BusinessCalendarService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * Check if a given date is a business day
     */
    public boolean isBusinessDay(LocalDate date) {
        return isBusinessDay(date, DEFAULT_WEEKEND_DAYS, DEFAULT_HOLIDAYS);
    }

    /**
     * Check if a given date is a business day for a specific tenant
     */
    public boolean isBusinessDay(LocalDate date, String tenantCode) {
        Set<LocalDate> holidays = getTenantHolidays(tenantCode);
        Set<DayOfWeek> weekendDays = getTenantWeekendDays(tenantCode);

        return isBusinessDay(date, weekendDays, holidays);
    }

    /**
     * Calculate the number of business days between two dates
     */
    public long calculateBusinessDays(LocalDate start, LocalDate end) {
        return calculateBusinessDays(start, end, DEFAULT_WEEKEND_DAYS, DEFAULT_HOLIDAYS);
    }

    /**
     * Calculate business days for a specific tenant
     */
    public long calculateBusinessDays(LocalDate start, LocalDate end, String tenantCode) {
        Set<LocalDate> holidays = getTenantHolidays(tenantCode);
        Set<DayOfWeek> weekendDays = getTenantWeekendDays(tenantCode);

        return calculateBusinessDays(start, end, weekendDays, holidays);
    }

    /**
     * Add business days to a date
     */
    public LocalDate addBusinessDays(LocalDate startDate, int businessDays) {
        return addBusinessDays(startDate, businessDays, DEFAULT_WEEKEND_DAYS, DEFAULT_HOLIDAYS);
    }

    /**
     * Add business days for a specific tenant
     */
    public LocalDate addBusinessDays(LocalDate startDate, int businessDays, String tenantCode) {
        Set<LocalDate> holidays = getTenantHolidays(tenantCode);
        Set<DayOfWeek> weekendDays = getTenantWeekendDays(tenantCode);

        return addBusinessDays(startDate, businessDays, weekendDays, holidays);
    }

    /**
     * Calculate due date for a task based on start time and business days
     */
    public LocalDateTime calculateDueDate(LocalDateTime startDateTime, int businessDays) {
        LocalDate newDate = addBusinessDays(startDateTime.toLocalDate(), businessDays,
                DEFAULT_WEEKEND_DAYS, DEFAULT_HOLIDAYS);
        return LocalDateTime.of(newDate, startDateTime.toLocalTime());
    }

    /**
     * Calculate due date for a specific tenant
     */
    public LocalDateTime calculateDueDate(LocalDateTime startDateTime, int businessDays, String tenantCode) {
        Set<LocalDate> holidays = getTenantHolidays(tenantCode);
        Set<DayOfWeek> weekendDays = getTenantWeekendDays(tenantCode);

        LocalDate newDate = addBusinessDays(startDateTime.toLocalDate(), businessDays, weekendDays, holidays);
        return LocalDateTime.of(newDate, startDateTime.toLocalTime());
    }

    /**
     * Check if a due date is overdue
     */
    public boolean isOverdue(LocalDateTime dueDate) {
        return LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Check if a due date is overdue for a specific tenant (accounting for business days)
     */
    public boolean isOverdue(LocalDateTime dueDate, String tenantCode) {
        // For simplicity, we'll just check if current time is past due date
        // In a more sophisticated implementation, you could factor in business hours
        return LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Calculate SLA (business hours) between two timestamps
     */
    public long calculateSLA(LocalDateTime start, LocalDateTime end) {
        return calculateSLA(start, end, DEFAULT_WEEKEND_DAYS, DEFAULT_HOLIDAYS);
    }

    /**
     * Calculate SLA for a specific tenant
     */
    public long calculateSLA(LocalDateTime start, LocalDateTime end, String tenantCode) {
        Set<LocalDate> holidays = getTenantHolidays(tenantCode);
        Set<DayOfWeek> weekendDays = getTenantWeekendDays(tenantCode);

        return calculateSLA(start, end, weekendDays, holidays);
    }

    /**
     * Get the next business day after a given date
     */
    public LocalDate getNextBusinessDay(LocalDate date) {
        return getNextBusinessDay(date, DEFAULT_WEEKEND_DAYS, DEFAULT_HOLIDAYS);
    }

    /**
     * Get the next business day for a specific tenant
     */
    public LocalDate getNextBusinessDay(LocalDate date, String tenantCode) {
        Set<LocalDate> holidays = getTenantHolidays(tenantCode);
        Set<DayOfWeek> weekendDays = getTenantWeekendDays(tenantCode);

        return getNextBusinessDay(date, weekendDays, holidays);
    }

    // Private helper methods

    private boolean isBusinessDay(LocalDate date, Set<DayOfWeek> weekendDays, Set<LocalDate> holidays) {
        return !weekendDays.contains(date.getDayOfWeek()) && !holidays.contains(date);
    }

    private long calculateBusinessDays(LocalDate start, LocalDate end, Set<DayOfWeek> weekendDays, Set<LocalDate> holidays) {
        long count = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (isBusinessDay(current, weekendDays, holidays)) {
                count++;
            }
            current = current.plusDays(1);
        }

        return count;
    }

    private LocalDate addBusinessDays(LocalDate start, int businessDays, Set<DayOfWeek> weekendDays, Set<LocalDate> holidays) {
        LocalDate current = start;
        int addedDays = 0;

        while (addedDays < businessDays) {
            current = current.plusDays(1);
            if (isBusinessDay(current, weekendDays, holidays)) {
                addedDays++;
            }
        }

        return current;
    }

    private long calculateSLA(LocalDateTime start, LocalDateTime end, Set<DayOfWeek> weekendDays, Set<LocalDate> holidays) {
        // Calculate business hours (9am-5pm) between dates
        long totalHours = 0;
        LocalDateTime current = start;

        while (current.isBefore(end)) {
            LocalDate currentDate = current.toLocalDate();
            if (isBusinessDay(currentDate, weekendDays, holidays)) {
                int hour = current.getHour();
                // Count only business hours (9am-5pm)
                if (hour >= 9 && hour < 17) {
                    totalHours++;
                }
            }
            current = current.plusHours(1);
        }

        return totalHours;
    }

    private LocalDate getNextBusinessDay(LocalDate date, Set<DayOfWeek> weekendDays, Set<LocalDate> holidays) {
        LocalDate current = date.plusDays(1);
        while (!isBusinessDay(current, weekendDays, holidays)) {
            current = current.plusDays(1);
        }
        return current;
    }

    private Set<LocalDate> getTenantHolidays(String tenantCode) {
        // In a real implementation, you would fetch tenant-specific holidays from the database
        // For now, return default holidays
        try {
            return tenantRepository.findByCode(tenantCode)
                    .map(tenant -> {
                        // Parse tenant-specific holidays if stored
                        // This is a placeholder - in production, you'd have a holidays table or JSON column
                        return DEFAULT_HOLIDAYS;
                    })
                    .orElse(DEFAULT_HOLIDAYS);
        } catch (Exception e) {
            logger.warn("Failed to fetch holidays for tenant {}, using defaults", tenantCode, e);
            return DEFAULT_HOLIDAYS;
        }
    }

    private Set<DayOfWeek> getTenantWeekendDays(String tenantCode) {
        // In a real implementation, you would fetch tenant-specific weekend days from the database
        // For now, return default weekend days (Saturday and Sunday)
        try {
            return tenantRepository.findByCode(tenantCode)
                    .map(tenant -> DEFAULT_WEEKEND_DAYS) // Placeholder for tenant-specific config
                    .orElse(DEFAULT_WEEKEND_DAYS);
        } catch (Exception e) {
            logger.warn("Failed to fetch weekend days for tenant {}, using defaults", tenantCode, e);
            return DEFAULT_WEEKEND_DAYS;
        }
    }
}
