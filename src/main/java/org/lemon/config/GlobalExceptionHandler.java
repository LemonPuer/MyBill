package org.lemon.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.resp.ApiResp;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 23:04:41
 */
@Slf4j
@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BindException.class)
    public ApiResp<String> bindException(BindException e) {
        log.error("请求参数异常: ", e);
        List<ObjectError> allErrors = e.getAllErrors();
        StringBuilder message = new StringBuilder();
        for (ObjectError temp : allErrors) {
            message.append(temp.getDefaultMessage()).append(",");
        }
        return ApiResp.fail(message.substring(0, message.length() - 1));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BusinessException.class)
    public ApiResp<String> businessException(BusinessException e) {
        log.error("业务异常: ", e);
        return ApiResp.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = Exception.class)
    public ApiResp<String> systemException(Exception e) {
        log.error("系统异常: ", e);
        return ApiResp.fail("系统异常, 请联系管理员");
    }
}
