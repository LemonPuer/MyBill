package org.lemon.entity.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/16 23:36:35
 */
@Data
@AllArgsConstructor
public class SimpleEnumVO {

    /**
     * 枚举key
     */
    private Integer key;

    /**
     * 枚举值
     */
    private String value;
}
