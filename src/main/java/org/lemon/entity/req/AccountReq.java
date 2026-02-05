package org.lemon.entity.req;

import lombok.Data;
import org.lemon.entity.Accounts;
import org.lemon.utils.UserUtil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/18 00:33:38
 */
@Data
public class AccountReq {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 父id
     */
    @NotNull(message = "父id不能为空！")
    private Integer pid;

    /**
     * 账户类型，AccountTypeEnum
     */
    @NotNull(message = "账户类型不能为空！")
    private Integer accountType;

    /**
     * 账户名称，xx银行卡
     */
    @NotBlank(message = "账户名称不能为空！")
    private String accountName;

    /**
     * 分类，余额/理财/活期等
     */
    @NotNull(message = "分类不能为空！")
    private Integer accountCategory;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空！")
    private Double amount;

    public Accounts toAccounts() {
        Accounts result = Accounts.builder()
                .userId(UserUtil.getCurrentUserId())
                .accountType(accountType)
                .accountName(accountName)
                .accountCategory(accountCategory)
                .build();
        result.setCreateNo(UserUtil.getCurrentUserId());
        if (amount != null && amount > 0) {
            result.setAmount(BigDecimal.valueOf(amount));
        } else {
            result.setAmount(BigDecimal.ZERO);
        }
        if (id != null && id > 0) {
            result.setId(id);
        }
        if (pid != 0 && pid > 0) {
            result.setPid(pid);
            result.setHierarchy(2);
        } else {
            result.setHierarchy(1);
        }
        return result;
    }
}
