package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 00:00:30
 */
@Data
@Builder
public class EmailRemainVO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 提醒名称
     */
    private String title;

    /**
     * 金额
     */
    private String amount;

    /**
     * 提醒类型;EmailRemainEnum
     */
    private Integer type;

    /**
     * 提醒类型；EmailRemainEnum
     */
    private String typeStr;

    /**
     * 提醒时间
     */
    private Date remainDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 相差时间(小时)
     */
    private Long durationBetweenNow;
}
