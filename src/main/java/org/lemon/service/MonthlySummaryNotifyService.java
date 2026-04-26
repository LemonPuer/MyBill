package org.lemon.service;

import lombok.RequiredArgsConstructor;
import org.lemon.entity.MonthTotalRecord;
import org.lemon.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlySummaryNotifyService {

    private final NotifyPreferenceService notifyPreferenceService;

    private final MonthTotalRecordService monthTotalRecordService;

    private final NotifyRecordService notifyRecordService;

    public void generatePendingRecords(LocalDateTime now, String actionUrl) {
        List<User> users = notifyPreferenceService.queryMonthlySummaryUsers();
        if (users.isEmpty()) {
            return;
        }
        Set<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
        Map<Integer, MonthTotalRecord> summaryMap = monthTotalRecordService.queryPreviousMonthSummaryByUserIds(userIds);
        users.forEach(user -> {
            MonthTotalRecord summary = summaryMap.get(user.getId());
            if (summary == null) {
                return;
            }
            String effectiveMonth = resolveMonthLabel(summary, now);
            notifyRecordService.createMonthlySummaryPendingRecord(user, effectiveMonth, now,
                    buildPayload(user, summary, actionUrl, effectiveMonth));
        });
    }

    Map<String, Object> buildPayload(User user, MonthTotalRecord summary, String actionUrl) {
        return buildPayload(user, summary, actionUrl, resolveMonthLabel(summary, null));
    }

    private Map<String, Object> buildPayload(User user, MonthTotalRecord summary, String actionUrl, String effectiveMonth) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", user.getUsername());
        payload.put("monthLabel", effectiveMonth);
        payload.put("totalIncome", NotifyRecordService.formatAmount(summary.getTotalIncome()));
        payload.put("totalExpense", NotifyRecordService.formatAmount(summary.getTotalExpense()));
        payload.put("totalBalance", NotifyRecordService.formatAmount(summary.getTotalBalance()));
        payload.put("actionUrl", actionUrl);
        return payload;
    }

    private String resolveMonthLabel(MonthTotalRecord summary, LocalDateTime now) {
        if (summary.getMonth() != null && !summary.getMonth().isBlank()) {
            return summary.getMonth();
        }
        YearMonth previousMonth = YearMonth.from(now == null ? LocalDateTime.now() : now).minusMonths(1);
        return previousMonth.toString();
    }
}
