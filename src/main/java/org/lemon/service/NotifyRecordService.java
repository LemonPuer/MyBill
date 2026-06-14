package org.lemon.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.NotifyRecord;
import org.lemon.entity.User;
import org.lemon.enumeration.NotifyBizTypeEnum;
import org.lemon.enumeration.NotifyChannelEnum;
import org.lemon.enumeration.NotifyRecordStatusEnum;
import org.lemon.mapper.NotifyRecordMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 通知发送记录表 服务层实现。
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Slf4j
@Service
public class NotifyRecordService extends ServiceImpl<NotifyRecordMapper, NotifyRecord> {

    public static final String DAILY_BOOKKEEPING_TEMPLATE_CODE = "DAILY_BOOKKEEPING_REMINDER";
    public static final String MONTHLY_SUMMARY_TEMPLATE_CODE = "MONTHLY_SUMMARY";
    private static final int MAX_RETRY_COUNT = 3;

    public NotifyRecord createDailyBookkeepingPendingRecord(User user, LocalDate bizDate, LocalDateTime scheduledTime,
                                                            Map<String, Object> payload) {
        String bizKey = buildDailyBookkeepingBizKey(user.getId(), bizDate);
        NotifyRecord existing = getByBizKeyAndChannel(bizKey, NotifyChannelEnum.EMAIL.name());
        if (existing != null) {
            return existing;
        }
        NotifyRecord record = NotifyRecord.builder()
                .userId(user.getId())
                .templateCode(DAILY_BOOKKEEPING_TEMPLATE_CODE)
                .bizType(NotifyBizTypeEnum.DAILY_BOOKKEEPING.name())
                .channel(NotifyChannelEnum.EMAIL.name())
                .target(user.getEmail())
                .bizKey(bizKey)
                .bizDate(bizDate)
                .payloadJson(JSON.toJSONString(payload == null ? Collections.emptyMap() : payload))
                .status(NotifyRecordStatusEnum.PENDING.getCode())
                .scheduledTime(scheduledTime)
                .retryCount(0)
                .build();
        try {
            save(record);
            return record;
        } catch (DuplicateKeyException e) {
            log.info("通知记录已存在，bizKey={} channel={}", bizKey, NotifyChannelEnum.EMAIL.name());
            return getByBizKeyAndChannel(bizKey, NotifyChannelEnum.EMAIL.name());
        }
    }

    public List<NotifyRecord> queryDueSendableRecords(LocalDateTime now) {
        return queryChain()
                .le(NotifyRecord::getScheduledTime, now)
                .in(NotifyRecord::getStatus,
                        NotifyRecordStatusEnum.PENDING.getCode(),
                        NotifyRecordStatusEnum.FAILED.getCode())
                .list().stream()
                .filter(this::isSendableRecord)
                .toList();
    }

    public NotifyRecord createMonthlySummaryPendingRecord(User user, String month, LocalDateTime scheduledTime,
                                                          Map<String, Object> payload) {
        String bizKey = buildMonthlySummaryBizKey(user.getId(), month);
        NotifyRecord existing = getByBizKeyAndChannel(bizKey, NotifyChannelEnum.EMAIL.name());
        if (existing != null) {
            return existing;
        }
        NotifyRecord record = NotifyRecord.builder()
                .userId(user.getId())
                .templateCode(MONTHLY_SUMMARY_TEMPLATE_CODE)
                .bizType(NotifyBizTypeEnum.MONTHLY_SUMMARY.name())
                .channel(NotifyChannelEnum.EMAIL.name())
                .target(user.getEmail())
                .bizKey(bizKey)
                .bizDate(parseMonthBizDate(month))
                .payloadJson(JSON.toJSONString(payload == null ? Collections.emptyMap() : payload))
                .status(NotifyRecordStatusEnum.PENDING.getCode())
                .scheduledTime(scheduledTime)
                .retryCount(0)
                .build();
        try {
            save(record);
            return record;
        } catch (DuplicateKeyException e) {
            log.info("通知记录已存在，bizKey={} channel={}", bizKey, NotifyChannelEnum.EMAIL.name());
            return getByBizKeyAndChannel(bizKey, NotifyChannelEnum.EMAIL.name());
        }
    }

    public void markSent(Long recordId, String subjectSnapshot, String contentSnapshot) {
        updateChain().eq(NotifyRecord::getId, recordId)
                .set(NotifyRecord::getStatus, NotifyRecordStatusEnum.SENT.getCode())
                .set(NotifyRecord::getSubjectSnapshot, subjectSnapshot)
                .set(NotifyRecord::getContentSnapshot, contentSnapshot)
                .set(NotifyRecord::getErrorMessage, null)
                .set(NotifyRecord::getSentTime, LocalDateTime.now())
                .update();
    }

    public void markFailed(NotifyRecord record, String subjectSnapshot, String contentSnapshot, String errorMessage) {
        updateChain().eq(NotifyRecord::getId, record.getId())
                .set(NotifyRecord::getStatus, NotifyRecordStatusEnum.FAILED.getCode())
                .set(NotifyRecord::getSubjectSnapshot, subjectSnapshot)
                .set(NotifyRecord::getContentSnapshot, contentSnapshot)
                .set(NotifyRecord::getErrorMessage, trimErrorMessage(errorMessage))
                .set(NotifyRecord::getRetryCount, getNextRetryCount(record.getRetryCount()))
                .update();
    }

    public Map<String, Object> parsePayload(String payloadJson) {
        if (StrUtil.isBlank(payloadJson)) {
            return Collections.emptyMap();
        }
        JSONObject jsonObject = JSONObject.parseObject(payloadJson);
        return jsonObject == null ? Collections.emptyMap() : jsonObject;
    }

    public static String buildDailyBookkeepingBizKey(Integer userId, LocalDate bizDate) {
        return "daily-bookkeeping:" + userId + ":" + bizDate;
    }

    public static String buildMonthlySummaryBizKey(Integer userId, String month) {
        return "monthly-summary:" + userId + ":" + month;
    }

    public static String formatAmount(BigDecimal amount) {
        return amount == null ? "0" : amount.stripTrailingZeros().toPlainString();
    }

    private NotifyRecord getByBizKeyAndChannel(String bizKey, String channel) {
        return queryChain()
                .eq(NotifyRecord::getBizKey, bizKey)
                .eq(NotifyRecord::getChannel, channel)
                .one();
    }

    private LocalDate parseMonthBizDate(String month) {
        if (StrUtil.isBlank(month)) {
            return null;
        }
        return LocalDate.parse(month + "-01");
    }

    private int getNextRetryCount(Integer retryCount) {
        return retryCount == null ? 1 : retryCount + 1;
    }

    private String trimErrorMessage(String errorMessage) {
        if (StrUtil.isBlank(errorMessage)) {
            return "通知发送失败";
        }
        return errorMessage.length() > 1000 ? errorMessage.substring(0, 1000) : errorMessage;
    }

    private boolean isSendableRecord(NotifyRecord record) {
        if (record == null || record.getStatus() == null) {
            return false;
        }
        if (NotifyRecordStatusEnum.PENDING.getCode().equals(record.getStatus())) {
            return true;
        }
        return NotifyRecordStatusEnum.FAILED.getCode().equals(record.getStatus())
                && getNextRetryCount(record.getRetryCount()) <= MAX_RETRY_COUNT;
    }
}
