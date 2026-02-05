package org.lemon.entity.req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/11/09 17:04:51
 */
@Data
public class EmailSendReq {

    /**
     * 收件人邮箱
     */
    @NotBlank(message = "收件人不能为空！")
    @Email(message = "收件人邮箱格式不正确！")
    private String targetEmail;

    /**
     * 密送邮箱
     */
    private String bccEmail;

    /**
     * 邮件主题
     */
    @NotBlank(message = "邮件主题不能为空！")
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 邮件内容（文件形式，支持html等）
     */
    private MultipartFile contentFile;

    /**
     * 附件
     */
    private List<MultipartFile> attachment;
}
