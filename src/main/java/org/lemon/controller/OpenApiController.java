package org.lemon.controller;

import lombok.AllArgsConstructor;
import org.lemon.entity.req.EmailSendReq;
import org.lemon.entity.common.ApiResp;
import org.lemon.service.EmailSendService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/11/09 16:48:50
 */
@RestController
@RequestMapping("openApi")
@AllArgsConstructor
public class OpenApiController {
    private EmailSendService emailSendService;

    @PostMapping("sendEmail")
    public ApiResp<Boolean> send(@Valid EmailSendReq req) {
        return ApiResp.ok(emailSendService.sendEmail(req));
    }
}
