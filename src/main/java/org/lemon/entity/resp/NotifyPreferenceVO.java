package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 通知偏好返回
 */
@Data
@Builder
public class NotifyPreferenceVO {

    /**
     * 是否开启每日记账邮件提醒
     */
    private Boolean emailReminderEnabled;

    /**
     * 是否开启月度摘要通知
     */
    private Boolean monthlySummaryEnabled;

    /**
     * 每日提醒发送小时
     */
    private Integer reminderSendHour;
}
