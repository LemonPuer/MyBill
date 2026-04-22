package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 通知偏好返回
 */
@Data
@Builder
public class NotifyPreferenceVO {

    private Boolean emailReminderEnabled;

    private Boolean monthlySummaryEnabled;

    private Integer reminderSendHour;
}
