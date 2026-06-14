package org.lemon.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.NotifyTemplate;
import org.lemon.entity.dto.NotifyRenderDTO;
import org.lemon.entity.exception.BusinessException;
import org.lemon.mapper.NotifyTemplateMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

/**
 * 通知模板表 服务层实现。
 *
 * @author Lemon
 * @since 2026-04-22
 */
@Slf4j
@Service
@AllArgsConstructor
public class NotifyTemplateService extends ServiceImpl<NotifyTemplateMapper, NotifyTemplate> {

    private static final freemarker.template.Configuration FREEMARKER_CONFIG = createFreeMarkerConfiguration();

    private final NotifyTemplateMapper notifyTemplateMapper;

    public NotifyRenderDTO renderByTemplateCode(String templateCode, Map<String, Object> templateParams) {
        if (StrUtil.isBlank(templateCode)) {
            throw new BusinessException("通知模板编码不能为空！");
        }
        NotifyTemplate template = QueryChain.of(notifyTemplateMapper)
                .eq(NotifyTemplate::getTemplateCode, templateCode)
                .eq(NotifyTemplate::getEnabled, Boolean.TRUE)
                .one();
        if (template == null) {
            throw new BusinessException("通知模板不存在或未启用！");
        }
        return renderTemplate(FREEMARKER_CONFIG, template, templateParams);
    }

    static freemarker.template.Configuration createFreeMarkerConfiguration() {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_34);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setFallbackOnNullLoopVariable(false);
        return configuration;
    }

    static NotifyRenderDTO renderTemplate(freemarker.template.Configuration configuration, NotifyTemplate template,
                                          Map<String, Object> templateParams) {
        if (template == null) {
            throw new BusinessException("通知模板不存在！");
        }
        return NotifyRenderDTO.builder()
                .subject(renderContent(configuration, template.getTemplateCode() + "-subject", template.getSubjectTemplate(), templateParams))
                .content(renderContent(configuration, template.getTemplateCode() + "-content", template.getContentTemplate(), templateParams))
                .build();
    }

    private static String renderContent(freemarker.template.Configuration configuration, String templateName,
                                        String templateContent, Map<String, Object> templateParams) {
        if (StrUtil.isBlank(templateContent)) {
            throw new BusinessException("通知模板内容不能为空！");
        }
        Map<String, Object> renderData = MapUtil.isEmpty(templateParams) ? Collections.emptyMap() : templateParams;
        try (StringWriter writer = new StringWriter()) {
            Template template = new Template(templateName, new StringReader(templateContent), configuration);
            template.process(renderData, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            log.error("通知模板渲染失败，templateName={}。", templateName, e);
            throw new BusinessException("通知模板渲染失败！");
        }
    }
}
