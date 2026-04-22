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
 * 通知模板表 实体类。
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_notify_template")
public class NotifyTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 发送渠道
     */
    private String channel;

    private String subjectTemplate;

    private String contentTemplate;

    private Boolean enabled;

    private String remark;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
