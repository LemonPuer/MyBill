package org.lemon.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 渲染通知模板后的主题和正文快照。
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyRenderDTO {

    /**
     * 渲染后的邮件主题。
     */
    private String subject;

    /**
     * 渲染后的邮件正文。
     */
    private String content;
}
