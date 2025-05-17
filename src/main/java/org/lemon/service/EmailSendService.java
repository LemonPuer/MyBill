package org.lemon.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.DTO.SystemEmailDTO;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.req.EmailSendReq;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 邮件发送服务
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/11/09 16:56:57
 */
@Slf4j
@Service
@AllArgsConstructor
public class EmailSendService {

    private final JavaMailSender javaMailSender;
    private final PropertiesService propertiesService;
    private final ResourceLoader resourceLoader;

    private static final String TEMPLATE_PATH = "classpath:email/";

    public boolean sendEmail(EmailSendReq req) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(req.getTargetEmail());
            helper.setSubject(req.getSubject());
            if (StrUtil.isNotBlank(req.getContent())) {
                helper.setText(req.getContent());
            } else {
                MultipartFile contentFile = req.getContentFile();
                if (contentFile == null) {
                    throw new BusinessException("邮件内容不能为空！");
                }
                if (Objects.requireNonNull(contentFile.getOriginalFilename()).endsWith(".html")) {
                    String content = new String(contentFile.getBytes(), StandardCharsets.UTF_8);
                    helper.setText(content, true);
                } else {
                    throw new BusinessException("邮件内容文件不合法！");
                }
            }
            if (CollUtil.isNotEmpty(req.getAttachment())) {
                for (MultipartFile file : req.getAttachment()) {
                    helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
                }
            }
            helper.setFrom(propertiesService.getMyEmailAccount());
            javaMailSender.send(mimeMessage);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("邮件发送失败：", e);
        }
        return false;
    }

    /**
     * 系统发送通知邮件
     *
     * @param dto
     */
    public void sendSystemEmail(SystemEmailDTO dto) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        Resource resource = resourceLoader.getResource(TEMPLATE_PATH + dto.getFileName());
        try {
            String content = FileUtil.readUtf8String(resource.getFile());
            // 2. 替换模板中的占位符
            for (Map.Entry<String, String> entry : dto.getTemplateParams().entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(dto.getTargetEmail());
            helper.setSubject(dto.getSubject());
            helper.setText(content, true);
            helper.setFrom(propertiesService.getMyEmailAccount());
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("系统发送邮件失败：", e);
        }
    }

}