package org.lemon.controller;

import org.lemon.entity.req.ApiReq;
import org.lemon.entity.resp.ApiResp;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/02 13:03:59
 */
@RestController
@RequestMapping("openApi/test")
public class TestController {
    @PostMapping("hello")
    public ApiResp<String> test(@RequestBody ApiReq<String> req) {
        return ApiResp.ok("hello " + req.getData());
    }

    @GetMapping("hi")
    public ApiResp<String> test() {
        return ApiResp.ok("hello world");
    }

    @GetMapping("csrf")
    public ApiResp<CsrfToken> csrf(CsrfToken token) {
        return ApiResp.ok(token);
    }

}
