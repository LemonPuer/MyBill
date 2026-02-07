package org.lemon.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lemon.entity.common.ApiResp;
import org.lemon.entity.common.TelegramBillMessageParam;
import org.lemon.entity.dto.ChatFinanceTransactionsDTO;
import org.lemon.service.FinanceTransactionsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/11/09 16:48:50
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("openApi")
public class OpenApiController {

    private final FinanceTransactionsService financeTransactionsService;

    /**
     * 处理Telegram ai bill请求
     *
     * @return
     */
    @PostMapping("/telegram/billMessage")
    public ApiResp<?> handleWebhook(@RequestBody TelegramBillMessageParam param) {
        financeTransactionsService.saveMessageBill(param);
        return ApiResp.ok();
    }
}
