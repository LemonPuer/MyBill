package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lemon.enumeration.RetryTaskStatusEnum;
import org.lemon.enumeration.RetryTaskTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI任务失败重试表 实体类。
 *
 * @author Lemon
 * @since 2026-02-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_retry_task")
public class RetryTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 任务类型（如：TELEGRAM_BILL_PARSE）
     */
    private RetryTaskTypeEnum taskType;

    /**
     * 任务数据（JSON格式）
     */
    private String taskData;

    /**
     * 关联用户ID
     */
    private Integer userId;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 状态：0-待重试，1-重试中，2-重试成功，3-重试失败
     */
    private RetryTaskStatusEnum status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 成功结果（JSON格式）
     */
    private String successResult;

    /**
     * 通知状态：0-未通知，1-已通知
     */
    private Boolean notifyStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

}
