package org.lemon.entity.common;

import com.mybatisflex.core.paginate.Page;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/10 10:56:15
 */
@Data
@Accessors(chain = true)
public class PageResp<T> {
    private Integer code;

    private String msg;

    private SimplePageDTO<T> data;


    public static <T> PageResp<T> ok(Page<T> page) {
        return new PageResp<T>().setCode(200).setMsg("ok").setData(new SimplePageDTO<>(page));
    }

    public static <T> PageResp<T> ok(SimplePageDTO<T> data) {
        return new PageResp<T>().setCode(200).setMsg("ok").setData(data);
    }

    public static <T> PageResp<T> fail(String msg) {
        return new PageResp<T>().setCode(500).setMsg(msg);
    }

}
