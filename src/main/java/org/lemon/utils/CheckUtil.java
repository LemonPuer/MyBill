package org.lemon.utils;

import cn.hutool.core.util.StrUtil;
import org.lemon.entity.exception.BusinessException;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 23:18:56
 */
public class CheckUtil {

    public static final String CHECK_FAIL_MSG = "请补充必填字段！";

    /**
     * 校验字段
     *
     * @param msg
     * @param obj
     */
    public static void checkField(String msg, Object... obj) {
        if (StrUtil.isBlank(msg)) {
            msg = CHECK_FAIL_MSG;
        }
        for (Object o : obj) {
            if (StrUtil.isBlankIfStr(o)) {
                throw new BusinessException(msg);
            }
        }
    }
}
