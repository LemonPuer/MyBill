package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知偏好表 实体类。
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_notify_preference")
public class NotifyPreference implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 通知业务类型
     */
    private String bizType;

    /**
     * 通知渠道
     */
    private String channel;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 发送小时
     */
    private Integer sendHour;

    /**
     * 扩展配置
     */
    private String extraConfigJson;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
