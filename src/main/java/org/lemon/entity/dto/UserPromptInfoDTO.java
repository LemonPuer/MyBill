package org.lemon.entity.dto;

import lombok.Data;
import org.lemon.entity.resp.MonthTotalRecordVO;
import org.lemon.entity.resp.SimpleEnumVO;

import java.util.List;

/**
 * 用户填充提示词信息
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/07 11:01:21
 */
@Data
public class UserPromptInfoDTO {

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 收支分类
     */
    private List<SimpleEnumVO> categories;

    /**
     * 月度总收支记录
     */
    private List<MonthTotalRecordVO> monthTotalRecords;

}
