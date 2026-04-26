package org.lemon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.User;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryNotifyServiceTest {

    @Mock
    private NotifyPreferenceService notifyPreferenceService;

    @Mock
    private MonthTotalRecordService monthTotalRecordService;

    @Mock
    private NotifyRecordService notifyRecordService;

    @InjectMocks
    private MonthlySummaryNotifyService monthlySummaryNotifyService;

    @Test
    void generatePendingRecordsCreatesARecordWhenSummaryExists() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 1, 9, 0);
        String actionUrl = "https://app.example.com/statistics/monthly";
        User firstUser = User.builder().id(1).username("amy").email("amy@example.com").build();
        MonthTotalRecord summary = MonthTotalRecord.builder()
                .userId(1)
                .month("2026-03")
                .totalIncome(new BigDecimal("1000.00"))
                .totalExpense(new BigDecimal("250.00"))
                .totalBalance(new BigDecimal("750.00"))
                .build();
        when(notifyPreferenceService.queryMonthlySummaryUsers()).thenReturn(java.util.List.of(firstUser));
        when(monthTotalRecordService.queryPreviousMonthSummaryByUserIds(Set.of(1))).thenReturn(Map.of(1, summary));

        monthlySummaryNotifyService.generatePendingRecords(now, actionUrl);

        verify(notifyRecordService).createMonthlySummaryPendingRecord(eq(firstUser), eq("2026-03"), eq(now), anyMap());
    }

    @Test
    void generatePendingRecordsUsesPreviousMonthFromMethodNowWhenSummaryMonthMissing() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 9, 0);
        String expectedMonth = YearMonth.from(now).minusMonths(1).toString();
        User user = User.builder().id(1).username("amy").email("amy@example.com").build();
        MonthTotalRecord summary = MonthTotalRecord.builder()
                .userId(1)
                .month(null)
                .totalIncome(new BigDecimal("1000.00"))
                .totalExpense(new BigDecimal("250.00"))
                .totalBalance(new BigDecimal("750.00"))
                .build();
        when(notifyPreferenceService.queryMonthlySummaryUsers()).thenReturn(java.util.List.of(user));
        when(monthTotalRecordService.queryPreviousMonthSummaryByUserIds(Set.of(1))).thenReturn(Map.of(1, summary));

        monthlySummaryNotifyService.generatePendingRecords(now, "https://app.example.com/statistics/monthly");

        ArgumentCaptor<String> monthCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notifyRecordService).createMonthlySummaryPendingRecord(eq(user), monthCaptor.capture(), eq(now), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals(expectedMonth, monthCaptor.getValue());
        assertEquals(expectedMonth, payload.get("monthLabel"));
    }

    @Test
    void buildPayloadFormatsSummaryFields() {
        User user = User.builder().id(1).username("amy").email("amy@example.com").build();
        MonthTotalRecord summary = MonthTotalRecord.builder()
                .userId(1)
                .month("2026-03")
                .totalIncome(new BigDecimal("1000.00"))
                .totalExpense(new BigDecimal("250.50"))
                .totalBalance(new BigDecimal("749.50"))
                .build();

        Map<String, Object> payload = monthlySummaryNotifyService.buildPayload(user, summary, "https://app.example.com/statistics/monthly");

        assertEquals("amy", payload.get("userName"));
        assertEquals("2026-03", payload.get("monthLabel"));
        assertEquals("1000", payload.get("totalIncome"));
        assertEquals("250.5", payload.get("totalExpense"));
        assertEquals("749.5", payload.get("totalBalance"));
        assertEquals("https://app.example.com/statistics/monthly", payload.get("actionUrl"));
    }
}
