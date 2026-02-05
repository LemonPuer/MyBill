package org.lemon.entity.req;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Date;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 14:03:15
 */
@Data
public class EmailRemainReq {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 提醒名称
     */
    @NotBlank(message = "提醒名称不能为空!")
    private String title;

    /**
     * 金额
     */
    @NotBlank(message = "金额不能为空!")
    private String amount;

    /**
     * 提醒类型;EmailRemainEnum
     */
    @NotNull(message = "提醒类型不能为空!")
    private Integer type;

    /**
     * 提醒时间
     */
    @NotNull(message = "提醒时间不能为空!")
    private Date remainDate;

    /**
     * 备注
     */
    private String remark;

}
