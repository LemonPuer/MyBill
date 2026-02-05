package org.lemon.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.lemon.utils.UserUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;


/**
 * 日志拦截器
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/26 11:53:38
 */
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            String currentUsername = UserUtil.getCurrentUsername();
            String uri = request.getRequestURI();
            if (uri.contains("user/")) {
                // 避免泄露用户信息
                return;
            }
            // 为什么不放preHandle：没调用过getInputStream或getReader方法，数据还没真正缓存
            if (isJsonRequest(request)) {
                ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
                // 获取请求体内容
                byte[] contentAsByteArray = requestWrapper.getContentAsByteArray();
                String requestBody = new String(contentAsByteArray, requestWrapper.getCharacterEncoding());
                log.info("用户：{}，请求路径：{}，请求体：{}", currentUsername, uri, requestBody);
            }

            if (!isJsonResponse(response)) {
                return;
            }
            ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;
            // 获取响应体内容
            byte[] contentAsByteArray = responseWrapper.getContentAsByteArray();
            String responseBody = new String(contentAsByteArray);
            if (StrUtil.isNotBlank(responseBody) && JSONUtil.isTypeJSON(responseBody)) {
                responseBody = Optional.ofNullable(JSONObject.parseObject(responseBody).getString("data")).orElse("");
            }
            log.info("用户：{}，请求路径：{}，响应体：{}", currentUsername, uri, responseBody);
        } catch (Exception e) {
            log.error("日志打印失败：", e);
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    /**
     * 判断请求是否为 JSON
     *
     * @param request HTTP 请求
     * @return 如果请求体是 JSON，则返回 true；否则返回 false
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    /**
     * 判断响应是否为 JSON
     *
     * @param response HTTP 响应
     * @return 如果响应体是 JSON，则返回 true；否则返回 false
     */
    private boolean isJsonResponse(HttpServletResponse response) {
        String contentType = response.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

}
