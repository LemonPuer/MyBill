package org.lemon.entity.common;

import com.mybatisflex.core.paginate.Page;
import lombok.Data;

import java.util.List;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/10 10:59:17
 */
@Data
public class SimplePageDTO<T> {

    /**
     * 列表
     */
    private List<T> result;

    /**
     * 总数
     */
    private Long total;

    public SimplePageDTO(List<T> result, Long total) {
        this.result = result;
        this.total = total;
    }

    public SimplePageDTO(Page<T> page) {
        this.result = page.getRecords();
        this.total = page.getTotalRow();
    }
}
