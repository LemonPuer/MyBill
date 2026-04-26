package org.lemon.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * 系统邮件发送参数。
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 17:19:58
 */
@Data
public class SystemEmailDTO {

    /**
     * 收件人名称
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
     * 邮件模板文件名；当 {@link #content} 为空时使用。
     */
    private String fileName;

    /**
     * 已渲染的邮件正文；优先级高于模板文件。
     */
    private String content;

    /**
     * 模板占位符参数。
     */
    private Map<String, String> templateParams;
}
