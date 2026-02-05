package org.lemon.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 17:19:58
 */
@Data
public class SystemEmailDTO {

    /**
     * 收件人
     */
    private String targetUser;

    /**
     * 收件人邮箱
     */
    private String targetEmail;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件模板路径
     */
    private String fileName;

    /**
     * 需要替换的字符串
     */
    private Map<String, String> templateParams;
}
