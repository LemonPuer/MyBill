package org.lemon.entity.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2026/01/31 23:07:30
 */
@Data
public class TelegramBillMessageParam {

    /**
     * 更新id
     */
    @JsonProperty("update_id")
    private String updateId;

    /**
     * 频道消息
     */
    @JsonProperty("channel_post")
    private ChannelPost channelPost;

    public String getText() {
        if (channelPost == null) {
            return "";
        }
        return channelPost.getText();
    }

    public String getChatId() {
        if (channelPost == null) {
            return "";
        }
        return Optional.ofNullable(channelPost.getChat()).map(Chat::getId).orElse("");
    }

    public LocalDateTime getDate() {
        if (channelPost == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(channelPost.getDate()), ZoneId.systemDefault());
    }

    @Data
    static class ChannelPost {

        /**
         * 消息id
         */
        @JsonProperty("message_id")
        private String messageId;

        /**
         * 频道信息
         */
        private Chat chat;

        /**
         * 发送时间（秒级时间戳）
         */
        private Long date;

        /**
         * 消息内容
         */
        private String text;
    }

    @Data
    static class Chat {

        /**
         * 频道id
         */
        private String id;

        /**
         * 频道名称
         */
        private String title;

        /**
         * 类型（channel、群组）
         */
        private String type;
    }
}
