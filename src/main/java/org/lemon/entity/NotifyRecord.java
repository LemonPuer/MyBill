package org.lemon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 通知发送记录表 实体类。
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tt_notify_record")
public class NotifyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Integer userId;

    private String templateCode;

    private String bizType;

    private String channel;

    private String target;

    private String bizKey;

    private LocalDate bizDate;

    private String payloadJson;

    private Integer status;

    private LocalDateTime scheduledTime;

    private String subjectSnapshot;

    private String contentSnapshot;

    private Integer retryCount;

    private String errorMessage;

    private LocalDateTime sentTime;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
