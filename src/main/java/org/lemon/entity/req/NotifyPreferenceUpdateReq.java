package org.lemon.entity.req;

import lombok.Data;

/**
 * 通知偏好更新请求
 */
@Data
public class NotifyPreferenceUpdateReq {

    private Boolean emailReminderEnabled;

    private Boolean monthlySummaryEnabled;

    private Integer reminderSendHour;
}
