package org.lemon.entity.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 22:35:06
 */
@Data
@Accessors(chain = true)
public class ApiResp<T> {
    private Integer code;

    private String msg;

    private T data;

    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResp<T> ok() {
        return new ApiResp<T>().setCode(200).setMsg("ok");
    }

    public static <T> ApiResp<T> ok(T data) {
        return new ApiResp<T>().setCode(200).setData(data);
    }

    public static <T> ApiResp<T> fail(String msg) {
        return new ApiResp<T>().setCode(500).setMsg(msg);
    }
}
