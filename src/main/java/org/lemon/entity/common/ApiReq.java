package org.lemon.entity.common;

import lombok.Data;

import java.util.Map;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/26 13:12:42
 */
@Data
public class ApiReq<T> {

    private T data;

    private Map<String, Object> map;
}
