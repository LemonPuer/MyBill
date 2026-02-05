package org.lemon.config;

import org.lemon.config.filter.LogStreamFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/26 11:46:19
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LogStreamFilter> cachingFilterRegistration() {
        FilterRegistrationBean<LogStreamFilter> registration = new FilterRegistrationBean<>();
        // 注入过滤器
        registration.setFilter(new LogStreamFilter());
        // 拦截规则
        registration.addUrlPatterns("/*");
        // 过滤器名称
        registration.setName("logStreamFilter");
        return registration;
    }
}
