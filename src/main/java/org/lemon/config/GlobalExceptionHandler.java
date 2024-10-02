package org.lemon.config;

import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.exception.BusinessException;
import org.lemon.entity.resp.ApiResp;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    @ExceptionHandler(value = BusinessException.class)
    public ApiResp<String> storageSourceException(BusinessException e) {
        log.error("BusinessException: ", e);
        return ApiResp.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = Exception.class)
    public ApiResp<String> storageSourceException(Exception e) {
        log.error("BusinessException: ", e);
        return ApiResp.fail("系统异常, 请联系管理员");
    }
}
