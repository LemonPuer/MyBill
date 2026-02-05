package org.lemon.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/02/05 23:01:06
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class ChatModelConfig {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

}
