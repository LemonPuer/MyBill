package org.lemon.entity.resp;

import lombok.Data;

import java.util.List;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/06/08 22:01:59
 */
@Data
public class CommonDicVO {

    /**
     * 名称
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 子级
     */
    private List<CommonDicVO> children;

}
