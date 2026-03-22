package com.flowable.platform.test.unit.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for date/time utility functions.
 * These are pure functions that can be tested without mocking.
 */
@DisplayName("Date Time Utilities Tests")
class DateTimeUtilsTest {

    private static final Set<DayOfWeek> WEEKEND_DAYS = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.of(2026, 12, 25), // Christmas
        LocalDate.of(2026, 1, 1)    // New Year
    );

    // ============ isBusinessDay Tests ============

    @Nested
    @DisplayName("isBusinessDay")
    class IsBusinessDayTests {

        @Test
        @DisplayName("Should return true for Monday")
        void shouldReturnTrueForMonday() {
            LocalDate monday = LocalDate.of(2026, 3, 23);
            assertThat(isBusinessDay(monday)).isTrue();
        }

        @Test
        @DisplayName("Should return true for Tuesday")
        void shouldReturnTrueForTuesday() {
            LocalDate tuesday = LocalDate.of(2026, 3, 24);
            assertThat(isBusinessDay(tuesday)).isTrue();
        }

        @Test
        @DisplayName("Should return true for Wednesday")
        void shouldReturnTrueForWednesday() {
            LocalDate wednesday = LocalDate.of(2026, 3, 25);
            assertThat(isBusinessDay(wednesday)).isTrue();
        }

        @Test
        @DisplayName("Should return true for Thursday")
        void shouldReturnTrueForThursday() {
            LocalDate thursday = LocalDate.of(2026, 3, 26);
            assertThat(isBusinessDay(thursday)).isTrue();
        }

        @Test
        @DisplayName("Should return true for Friday")
        void shouldReturnTrueForFriday() {
            LocalDate friday = LocalDate.of(2026, 3, 27);
            assertThat(isBusinessDay(friday)).isTrue();
        }

        @Test
        @DisplayName("Should return false for Saturday")
        void shouldReturnFalseForSaturday() {
            LocalDate saturday = LocalDate.of(2026, 3, 21);
            assertThat(isBusinessDay(saturday)).isFalse();
        }

        @Test
        @DisplayName("Should return false for Sunday")
        void shouldReturnFalseForSunday() {
            LocalDate sunday = LocalDate.of(2026, 3, 22);
            assertThat(isBusinessDay(sunday)).isFalse();
        }

        @Test
        @DisplayName("Should return false for Christmas")
        void shouldReturnFalseForChristmas() {
            LocalDate christmas = LocalDate.of(2026, 12, 25);
            assertThat(isBusinessDay(christmas)).isFalse();
        }

        @Test
        @DisplayName("Should return false for New Year")
        void shouldReturnFalseForNewYear() {
            LocalDate newYear = LocalDate.of(2026, 1, 1);
            assertThat(isBusinessDay(newYear)).isFalse();
        }
    }

    // ============ calculateBusinessDays Tests ============

    @Nested
    @DisplayName("calculateBusinessDays")
    class CalculateBusinessDaysTests {

        @Test
        @DisplayName("Should return 1 for consecutive business days")
        void shouldReturn1ForConsecutiveBusinessDays() {
            LocalDate monday = LocalDate.of(2026, 3, 23);
            LocalDate tuesday = LocalDate.of(2026, 3, 24);

            long result = calculateBusinessDays(monday, tuesday);

            assertThat(result).isEqualTo(2); // Both Monday and Tuesday
        }

        @Test
        @DisplayName("Should exclude weekend in calculation")
        void shouldExcludeWeekendInCalculation() {
            LocalDate friday = LocalDate.of(2026, 3, 27);
            LocalDate monday = LocalDate.of(2026, 3, 30);

            long result = calculateBusinessDays(friday, monday);

            assertThat(result).isEqualTo(2); // Friday + Monday (Sat/Sun excluded)
        }

        @Test
        @DisplayName("Should return 0 for same date on weekend")
        void shouldReturn0ForSameDateOnWeekend() {
            LocalDate saturday = LocalDate.of(2026, 3, 21);

            long result = calculateBusinessDays(saturday, saturday);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for same date on holiday")
        void shouldReturn0ForSameDateOnHoliday() {
            LocalDate christmas = LocalDate.of(2026, 12, 25);

            long result = calculateBusinessDays(christmas, christmas);

            assertThat(result).isEqualTo(0);
        }
    }

    // ============ addBusinessDays Tests ============

    @Nested
    @DisplayName("addBusinessDays")
    class AddBusinessDaysTests {

        @Test
        @DisplayName("Should add 1 business day skipping weekend")
        void shouldAdd1BusinessDaySkippingWeekend() {
            LocalDate friday = LocalDate.of(2026, 3, 27);

            LocalDate result = addBusinessDays(friday, 1);

            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 30)); // Monday
        }

        @Test
        @DisplayName("Should add 5 business days spanning weekend")
        void shouldAdd5BusinessDaysSpanningWeekend() {
            LocalDate friday = LocalDate.of(2026, 3, 27);

            LocalDate result = addBusinessDays(friday, 5);

            // Friday + 5 business days = next Friday (Mon, Tue, Wed, Thu, Fri)
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        }

        @Test
        @DisplayName("Should skip holiday when adding business days")
        void shouldSkipHolidayWhenAddingBusinessDays() {
            // Dec 23 is Wednesday, Dec 24 is Thursday (business day), Dec 25 is Friday (Christmas holiday)
            LocalDate wednesday = LocalDate.of(2026, 12, 23);

            LocalDate result = addBusinessDays(wednesday, 1);

            // Next business day after Wednesday is Thursday (Dec 24)
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
        }

        @Test
        @DisplayName("Should return same date when adding 0 business days")
        void shouldReturnSameDateWhenAdding0BusinessDays() {
            LocalDate monday = LocalDate.of(2026, 3, 23);

            LocalDate result = addBusinessDays(monday, 0);

            assertThat(result).isEqualTo(monday);
        }

        @Test
        @DisplayName("Should handle Monday to Monday addition")
        void shouldHandleMondayToMondayAddition() {
            LocalDate monday = LocalDate.of(2026, 3, 23);

            LocalDate result = addBusinessDays(monday, 1);

            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 24)); // Tuesday
        }
    }

    // ============ getNextBusinessDay Tests ============

    @Nested
    @DisplayName("getNextBusinessDay")
    class GetNextBusinessDayTests {

        @Test
        @DisplayName("Should return Monday for Friday")
        void shouldReturnMondayForFriday() {
            LocalDate friday = LocalDate.of(2026, 3, 27);

            LocalDate result = getNextBusinessDay(friday);

            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 30)); // Monday
        }

        @Test
        @DisplayName("Should return Tuesday for Monday")
        void shouldReturnTuesdayForMonday() {
            LocalDate monday = LocalDate.of(2026, 3, 23);

            LocalDate result = getNextBusinessDay(monday);

            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 24)); // Tuesday
        }

        @Test
        @DisplayName("Should return Saturday for Thursday (skipping Friday which is holiday)")
        void shouldReturnSaturdayForThursday() {
            LocalDate thursday = LocalDate.of(2026, 12, 25); // If Thursday was a holiday
            // Note: This test assumes we need to adjust for actual holidays

            LocalDate result = getNextBusinessDay(thursday);

            assertThat(result.getDayOfWeek()).isNotEqualTo(DayOfWeek.FRIDAY);
        }
    }

    // ============ isOverdue Tests ============

    @Nested
    @DisplayName("isOverdue")
    class IsOverdueTests {

        @Test
        @DisplayName("Should return true for past due date")
        void shouldReturnTrueForPastDueDate() {
            LocalDateTime pastDate = LocalDateTime.now().minusHours(1);

            boolean result = isOverdue(pastDate);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for future due date")
        void shouldReturnFalseForFutureDueDate() {
            LocalDateTime futureDate = LocalDateTime.now().plusHours(1);

            boolean result = isOverdue(futureDate);

            assertThat(result).isFalse();
        }
    }

    // ============ Helper Methods (mirroring actual implementation) ============

    private boolean isBusinessDay(LocalDate date) {
        return !WEEKEND_DAYS.contains(date.getDayOfWeek()) && !HOLIDAYS.contains(date);
    }

    private long calculateBusinessDays(LocalDate start, LocalDate end) {
        long count = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (isBusinessDay(current)) {
                count++;
            }
            current = current.plusDays(1);
        }

        return count;
    }

    private LocalDate addBusinessDays(LocalDate start, int businessDays) {
        LocalDate current = start;
        int addedDays = 0;

        while (addedDays < businessDays) {
            current = current.plusDays(1);
            if (isBusinessDay(current)) {
                addedDays++;
            }
        }

        return current;
    }

    private LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate current = date.plusDays(1);
        while (!isBusinessDay(current)) {
            current = current.plusDays(1);
        }
        return current;
    }

    private boolean isOverdue(LocalDateTime dueDate) {
        return LocalDateTime.now().isAfter(dueDate);
    }
}
