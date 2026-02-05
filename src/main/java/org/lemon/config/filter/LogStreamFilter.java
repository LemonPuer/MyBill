package org.lemon.config.filter;

import cn.hutool.http.ContentType;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/26 11:41:54
 */
@Slf4j
public class LogStreamFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequestrequest, ServletResponse servletResponseresponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequestrequest;
        HttpServletResponse response = (HttpServletResponse) servletResponseresponse;
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        // 检查是否是文件上传请求
        if (request.getContentType() != null && request.getContentType().startsWith(ContentType.MULTIPART.getValue())) {
            filterChain.doFilter(request, responseWrapper);
            // 将内容写回原始响应
            responseWrapper.copyBodyToResponse();
            return;
        }
        // 转换为请求缓存对象和响应缓存对象
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 64 * 1024);
        filterChain.doFilter(requestWrapper, responseWrapper);
        responseWrapper.copyBodyToResponse();
    }
}
