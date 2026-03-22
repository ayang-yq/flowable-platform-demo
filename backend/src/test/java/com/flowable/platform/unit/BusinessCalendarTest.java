package com.flowable.platform.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessCalendar functionality.
 * Tests business day calculations excluding weekends and tenant-specific holidays.
 */
@DisplayName("BusinessCalendar Tests")
class BusinessCalendarTest {

    private Set<LocalDate> holidays;
    private Set<DayOfWeek> weekendDays;

    @BeforeEach
    void setUp() {
        // Setup holidays (e.g., Christmas, New Year)
        holidays = new HashSet<>();
        holidays.add(LocalDate.of(2026, 12, 25)); // Christmas
        holidays.add(LocalDate.of(2026, 1, 1));   // New Year's Day
        holidays.add(LocalDate.of(2026, 7, 4));   // Independence Day

        // Setup weekend days (Saturday and Sunday)
        weekendDays = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }

    @Test
    @DisplayName("isBusinessDay should return true for weekday")
    void isBusinessDay_weekday_returnsTrue() {
        LocalDate date = LocalDate.of(2026, 3, 21); // Saturday in 2026
        LocalDate weekday = LocalDate.of(2026, 3, 23); // Monday in 2026

        assertTrue(isBusinessDay(weekday, weekendDays, holidays));
    }

    @Test
    @DisplayName("isBusinessDay should return false for Saturday")
    void isBusinessDay_saturday_returnsFalse() {
        LocalDate saturday = LocalDate.of(2026, 3, 21); // Saturday

        assertFalse(isBusinessDay(saturday, weekendDays, holidays));
    }

    @Test
    @DisplayName("isBusinessDay should return false for Sunday")
    void isBusinessDay_sunday_returnsFalse() {
        LocalDate sunday = LocalDate.of(2026, 3, 22); // Sunday

        assertFalse(isBusinessDay(sunday, weekendDays, holidays));
    }

    @Test
    @DisplayName("isBusinessDay should return false for holiday")
    void isBusinessDay_holiday_returnsFalse() {
        LocalDate christmas = LocalDate.of(2026, 12, 25); // Saturday (Christmas)

        assertFalse(isBusinessDay(christmas, weekendDays, holidays));
    }

    @Test
    @DisplayName("calculateBusinessDays should count only business days")
    void calculateBusinessDays_normalWeek_returns5() {
        LocalDate start = LocalDate.of(2026, 3, 23); // Monday
        LocalDate end = LocalDate.of(2026, 3, 27);   // Friday

        long businessDays = calculateBusinessDays(start, end, weekendDays, holidays);

        assertEquals(5, businessDays);
    }

    @Test
    @DisplayName("calculateBusinessDays should exclude weekends")
    void calculateBusinessDays_withWeekend_returns3() {
        LocalDate start = LocalDate.of(2026, 3, 23); // Monday
        LocalDate end = LocalDate.of(2026, 3, 29);   // Sunday

        long businessDays = calculateBusinessDays(start, end, weekendDays, holidays);

        assertEquals(5, businessDays); // Mon-Fri only
    }

    @Test
    @DisplayName("calculateBusinessDays should exclude holidays")
    void calculateBusinessDays_withHoliday_returns4() {
        LocalDate start = LocalDate.of(2026, 12, 22); // Monday
        LocalDate end = LocalDate.of(2026, 12, 26);   // Friday (Christmas on Fri)

        long businessDays = calculateBusinessDays(start, end, weekendDays, holidays);

        assertEquals(4, businessDays); // Mon-Thu (Christmas excluded)
    }

    @Test
    @DisplayName("addBusinessDays should skip weekends")
    void addBusinessDays_add5Days_returnsMonday() {
        LocalDate start = LocalDate.of(2026, 3, 23); // Monday
        int businessDaysToAdd = 5;

        LocalDate result = addBusinessDays(start, businessDaysToAdd, weekendDays, holidays);

        assertEquals(LocalDate.of(2026, 3, 30), result); // Next Monday
    }

    @Test
    @DisplayName("addBusinessDays should skip holidays")
    void addBusinessDays_addDaysWithHoliday_returnsCorrectDate() {
        LocalDate start = LocalDate.of(2026, 12, 22); // Monday
        int businessDaysToAdd = 5;

        LocalDate result = addBusinessDays(start, businessDaysToAdd, weekendDays, holidays);

        // Mon 22, Tue 23, Wed 24, Thu 25 (holiday), Fri 26, Mon 29, Tue 30
        // 5 business days: Mon 22, Tue 23, Wed 24, Fri 26, Mon 29
        assertEquals(LocalDate.of(2026, 12, 29), result);
    }

    @Test
    @DisplayName("addBusinessDays with 0 days should return same date")
    void addBusinessDays_add0Days_returnsSameDate() {
        LocalDate start = LocalDate.of(2026, 3, 23);

        LocalDate result = addBusinessDays(start, 0, weekendDays, holidays);

        assertEquals(start, result);
    }

    @Test
    @DisplayName("calculateDueDate should return correct business day due date")
    void calculateDueDate_fromFriday_adds3BusinessDays_returnsWednesday() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 27, 10, 0); // Friday 10am
        int businessDaysToAdd = 3;

        LocalDateTime result = calculateDueDate(start, businessDaysToAdd, weekendDays, holidays);

        assertEquals(LocalDateTime.of(2026, 4, 1, 10, 0), result); // Next Wednesday 10am
    }

    @Test
    @DisplayName("isOverdue should check due date against current time")
    void isOverdue_pastDueDate_returnsTrue() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(1);

        assertTrue(isOverdue(dueDate));
    }

    @Test
    @DisplayName("isOverdue with future due date should return false")
    void isOverdue_futureDueDate_returnsFalse() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

        assertFalse(isOverdue(dueDate));
    }

    @Test
    @DisplayName("calculateSLA should return correct business hours between dates")
    void calculateSLA_twoBusinessDays_returns16Hours() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 23, 9, 0); // Monday 9am
        LocalDateTime end = LocalDateTime.of(2026, 3, 25, 9, 0);   // Wednesday 9am

        long slaHours = calculateSLA(start, end, weekendDays, holidays);

        assertEquals(16, slaHours); // 2 business days * 8 hours
    }

    @Test
    @DisplayName("calculateSLA should exclude non-business hours")
    void calculateSLA_withWeekend_returns8Hours() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 27, 17, 0); // Friday 5pm
        LocalDateTime end = LocalDateTime.of(2026, 3, 30, 10, 0);   // Monday 10am

        long slaHours = calculateSLA(start, end, weekendDays, holidays);

        assertEquals(1, slaHours); // Only 1 hour on Monday (9-10am)
    }

    @Test
    @DisplayName("getNextBusinessDay should return Monday if date is Friday")
    void getNextBusinessDay_friday_returnsMonday() {
        LocalDate friday = LocalDate.of(2026, 3, 28); // Friday

        LocalDate result = getNextBusinessDay(friday, weekendDays, holidays);

        assertEquals(LocalDate.of(2026, 3, 31), result); // Monday
    }

    @Test
    @DisplayName("getNextBusinessDay should skip holiday")
    void getNextBusinessDay_beforeHoliday_returnsDayAfterHoliday() {
        LocalDate dayBeforeChristmas = LocalDate.of(2026, 12, 24); // Thursday

        LocalDate result = getNextBusinessDay(dayBeforeChristmas, weekendDays, holidays);

        assertEquals(LocalDate.of(2026, 12, 26), result); // Monday (skip Christmas and weekend)
    }

    // Helper methods (these would be in the actual BusinessCalendarService)
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

    private LocalDateTime calculateDueDate(LocalDateTime start, int businessDays, Set<DayOfWeek> weekendDays, Set<LocalDate> holidays) {
        LocalDate newDate = addBusinessDays(start.toLocalDate(), businessDays, weekendDays, holidays);
        return LocalDateTime.of(newDate, start.toLocalTime());
    }

    private boolean isOverdue(LocalDateTime dueDate) {
        return LocalDateTime.now().isAfter(dueDate);
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
}
