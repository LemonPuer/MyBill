package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.EmailSendReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/11/09 16:56:57
 */
@Slf4j
@Service
public class EmailSendService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${email.account:${spring.mail.username}}")
    private String myEmailAccount;

    public boolean sendEmail(EmailSendReq req) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(myEmailAccount);
            helper.setTo(req.getTargetEmail());
            helper.setSubject(req.getSubject());
            setEmailContent(req, helper);
            if (CollUtil.isNotEmpty(req.getAttachment())) {
                for (MultipartFile file : req.getAttachment()) {
                    helper.addAttachment(file.getOriginalFilename(), file);
                }
            }
            javaMailSender.send(mimeMessage);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("邮件发送失败：", e);
        }
        return false;
    }

    private void setEmailContent(EmailSendReq req, MimeMessageHelper helper) throws Exception {
        if (StrUtil.isNotBlank(req.getContent())) {
            helper.setText(req.getContent());
            return;
        }
        MultipartFile contentFile = req.getContentFile();
        if (contentFile == null) {
            throw new BusinessException("邮件内容不能为空！");
        }
        if (Objects.requireNonNull(contentFile.getOriginalFilename()).endsWith(".html")) {
            String content = new String(contentFile.getBytes(), StandardCharsets.UTF_8);
            helper.setText(content, true);
            return;
        }
        throw new BusinessException("邮件内容文件不合法！");
    }
}