package org.lemon.entity.common;

/**
 * 分页请求参数
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 22:44:25
 */
public class BasePage {

    private Integer pageNum;

    private Integer pageSize;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
