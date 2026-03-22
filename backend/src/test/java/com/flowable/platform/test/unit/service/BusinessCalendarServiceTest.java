package com.flowable.platform.test.unit.service;

import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.service.BusinessCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BusinessCalendarService.
 * Tests business day calculations excluding weekends and holidays.
 */
@DisplayName("BusinessCalendarService Tests")
class BusinessCalendarServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private BusinessCalendarService businessCalendarService;

    @Nested
    @DisplayName("isBusinessDay")
    class IsBusinessDayTests {

        @Test
        @DisplayName("Should return true for weekday")
        void shouldReturnTrueForWeekday() {
            LocalDate monday = LocalDate.of(2026, 3, 23);

            boolean result = businessCalendarService.isBusinessDay(monday);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for Saturday")
        void shouldReturnFalseForSaturday() {
            LocalDate saturday = LocalDate.of(2026, 3, 21);

            boolean result = businessCalendarService.isBusinessDay(saturday);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for Sunday")
        void shouldReturnFalseForSunday() {
            LocalDate sunday = LocalDate.of(2026, 3, 22);

            boolean result = businessCalendarService.isBusinessDay(sunday);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for Christmas holiday")
        void shouldReturnFalseForChristmas() {
            LocalDate christmas = LocalDate.of(2026, 12, 25);

            boolean result = businessCalendarService.isBusinessDay(christmas);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("calculateBusinessDays")
    class CalculateBusinessDaysTests {

        @Test
        @DisplayName("Should count 5 business days for Monday to Friday")
        void shouldCount5BusinessDaysForMondayToFriday() {
            LocalDate start = LocalDate.of(2026, 3, 23); // Monday
            LocalDate end = LocalDate.of(2026, 3, 27);     // Friday

            long result = businessCalendarService.calculateBusinessDays(start, end);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Should exclude weekends from count")
        void shouldExcludeWeekendsFromCount() {
            LocalDate start = LocalDate.of(2026, 3, 23); // Monday
            LocalDate end = LocalDate.of(2026, 3, 29);   // Sunday

            long result = businessCalendarService.calculateBusinessDays(start, end);

            assertThat(result).isEqualTo(5); // Mon-Fri only
        }

        @Test
        @DisplayName("Should exclude holidays from count")
        void shouldExcludeHolidaysFromCount() {
            // Assuming Christmas 2026 is on a Wednesday
            LocalDate start = LocalDate.of(2026, 12, 21); // Monday
            LocalDate end = LocalDate.of(2026, 12, 25);   // Friday (Christmas on Wed)

            long result = businessCalendarService.calculateBusinessDays(start, end);

            assertThat(result).isEqualTo(4); // Mon-Tue, Thu-Fri (Wed excluded)
        }

        @Test
        @DisplayName("Should return 0 for same start and end date on weekend")
        void shouldReturn0ForSameDateOnWeekend() {
            LocalDate saturday = LocalDate.of(2026, 3, 21);

            long result = businessCalendarService.calculateBusinessDays(saturday, saturday);

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("addBusinessDays")
    class AddBusinessDaysTests {

        @Test
        @DisplayName("Should skip weekends when adding business days")
        void shouldSkipWeekendsWhenAddingBusinessDays() {
            LocalDate friday = LocalDate.of(2026, 3, 27);

            LocalDate result = businessCalendarService.addBusinessDays(friday, 1);

            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("Should skip holidays when adding business days")
        void shouldSkipHolidaysWhenAddingBusinessDays() {
            // Assuming Dec 24 is Thursday (Christmas Eve) and Dec 25 is Friday (Christmas)
            LocalDate wednesday = LocalDate.of(2026, 12, 23);

            LocalDate result = businessCalendarService.addBusinessDays(wednesday, 1);

            // Should skip to Monday (Dec 28)
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("Should return same date when adding 0 business days")
        void shouldReturnSameDateWhenAdding0BusinessDays() {
            LocalDate monday = LocalDate.of(2026, 3, 23);

            LocalDate result = businessCalendarService.addBusinessDays(monday, 0);

            assertThat(result).isEqualTo(monday);
        }

        @Test
        @DisplayName("Should handle adding multiple business days")
        void shouldHandleAddingMultipleBusinessDays() {
            LocalDate friday = LocalDate.of(2026, 3, 27);

            LocalDate result = businessCalendarService.addBusinessDays(friday, 5);

            // Friday + 5 business days = Thursday (2 weeks later)
            assertThat(result).isEqualTo(LocalDate.of(2026, 4, 3));
        }
    }

    @Nested
    @DisplayName("isOverdue")
    class IsOverdueTests {

        @Test
        @DisplayName("Should return true for past due date")
        void shouldReturnTrueForPastDueDate() {
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

            boolean result = businessCalendarService.isOverdue(pastDate);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for future due date")
        void shouldReturnFalseForFutureDueDate() {
            LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

            boolean result = businessCalendarService.isOverdue(futureDate);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getNextBusinessDay")
    class GetNextBusinessDayTests {

        @Test
        @DisplayName("Should return Monday for Friday")
        void shouldReturnMondayForFriday() {
            LocalDate friday = LocalDate.of(2026, 3, 27);

            LocalDate result = businessCalendarService.getNextBusinessDay(friday);

            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("Should return next day for Monday")
        void shouldReturnNextDayForMonday() {
            LocalDate monday = LocalDate.of(2026, 3, 23);

            LocalDate result = businessCalendarService.getNextBusinessDay(monday);

            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        }

        @Test
        @DisplayName("Should skip holiday and weekend")
        void shouldSkipHolidayAndWeekend() {
            // If Thursday is a holiday
            LocalDate wednesday = LocalDate.of(2026, 12, 24);

            LocalDate result = businessCalendarService.getNextBusinessDay(wednesday);

            // Should skip Thursday (holiday), Friday (weekend starts), Saturday, Sunday, Monday
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        }
    }
}
