package org.lemon.entity.resp;

import lombok.Builder;
import lombok.Data;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 00:37:54
 */
@Data
@Builder
public class AccountVO {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 父id
     */
    private Integer pid;

    /**
     * 账户类型，AccountTypeEnum
     */
    private String accountType;

    /**
     * 账户名称，xx银行卡
     */
    private String accountName;

    /**
     * 分类，余额/理财/活期等
     */
    private String accountCategory;

    /**
     * 金额
     */
    private Double amount;

    /**
     * 子账户
     */
    private Boolean children;
}
