package org.lemon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lemon.entity.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyBookkeepingNotifyServiceTest {

    @Mock
    private NotifyPreferenceService notifyPreferenceService;

    @Mock
    private NotifyRecordService notifyRecordService;

    @InjectMocks
    private DailyBookkeepingNotifyService dailyBookkeepingNotifyService;

    @Test
    void generatePendingRecordsCreatesARecordForEachMatchedUser() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 9, 30);
        String actionUrl = "https://app.example.com/bill/list";
        LocalDate bizDate = now.toLocalDate();
        User firstUser = User.builder().id(1).username("amy").email("amy@example.com").build();
        User secondUser = User.builder().id(2).username("bob").email("bob@example.com").build();
        when(notifyPreferenceService.queryDailyBookkeepingUsers(9)).thenReturn(List.of(firstUser, secondUser));

        dailyBookkeepingNotifyService.generatePendingRecords(now, actionUrl);

        verify(notifyRecordService).createDailyBookkeepingPendingRecord(eq(firstUser), eq(bizDate), eq(now), anyMap());
        verify(notifyRecordService).createDailyBookkeepingPendingRecord(eq(secondUser), eq(bizDate), eq(now), anyMap());
    }

    @Test
    void generatePendingRecordsSkipsUsersWithBlankEmail() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 9, 30);
        String actionUrl = "https://app.example.com/bill/list";
        LocalDate bizDate = now.toLocalDate();
        User validUser = User.builder().id(1).username("amy").email("amy@example.com").build();
        User blankEmailUser = User.builder().id(2).username("bob").email("   ").build();
        when(notifyPreferenceService.queryDailyBookkeepingUsers(9)).thenReturn(List.of(validUser, blankEmailUser));

        dailyBookkeepingNotifyService.generatePendingRecords(now, actionUrl);

        verify(notifyRecordService).createDailyBookkeepingPendingRecord(eq(validUser), eq(bizDate), eq(now), anyMap());
        verify(notifyRecordService, never()).createDailyBookkeepingPendingRecord(eq(blankEmailUser), eq(bizDate), eq(now), anyMap());
    }

    @Test
    void buildPayloadContainsExpectedTemplateFields() {
        User user = User.builder().id(1).username("amy").email("amy@example.com").build();
        LocalDate bizDate = LocalDate.of(2026, 4, 26);

        Map<String, Object> payload = dailyBookkeepingNotifyService.buildPayload(user, bizDate, "https://app.example.com/bill/list");

        assertEquals("amy", payload.get("userName"));
        assertEquals("https://app.example.com/bill/list", payload.get("actionUrl"));
        assertEquals("花了什么、赚了什么，顺手记一笔，月底复盘更轻松。", payload.get("encouragement"));
    }
}
