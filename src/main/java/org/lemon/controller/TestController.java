package org.lemon.controller;

import org.lemon.entity.common.ApiResp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("hi")
    public ApiResp<String> test() {
        return ApiResp.ok("hello world");
    }

}
