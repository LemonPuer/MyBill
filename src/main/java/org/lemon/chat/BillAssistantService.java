package org.lemon.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import org.lemon.entity.dto.ChatFinanceTransactionsDTO;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 系统使用模型（账单助手）
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/05 23:07:32
 */
@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel")
public interface BillAssistantService {


    @SystemMessage("{{prompt}}")
    ChatFinanceTransactionsDTO billMessageChat(@V("prompt") String systemPrompt, String message);
}
