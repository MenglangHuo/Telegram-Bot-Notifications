package com.bronx.telegram.notification.service.impl.webhookCommand.helper;

import com.bronx.telegram.notification.service.TelegramBotService;
import com.bronx.telegram.notification.service.impl.TelegramBotClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageSender {

    private final TelegramBotService botService;
    private final ObjectMapper objectMapper;

    public void sendMessage(Long botId, String chatId, String text) {
        try {
            TelegramBotClient client = botService.getBotClient(botId);
            if (client != null) {
                client.sendMessageWithReturn(chatId, text, false, "HTML", null);
            }
        } catch (Exception e) {
            log.error("Failed to send reply", e);
        }
    }

    public void sendMessageWithKeyboard(Long botId, String chatId, String text,
                                        List<List<String>> buttons) {
        try {
            TelegramBotClient client = botService.getBotClient(botId);
            ObjectNode keyboardNode = objectMapper.createObjectNode();
            ArrayNode keyboardArray = keyboardNode.putArray("keyboard");

            for (List<String> row : buttons) {
                ArrayNode rowNode = keyboardArray.addArray();
                row.forEach(rowNode::add);
            }
            keyboardNode.put("resize_keyboard", true);
            keyboardNode.put("one_time_keyboard", true);

            client.sendMessageWithKeyboard(chatId, text, "HTML", keyboardNode.toString());
        } catch (Exception e) {
            log.error("Failed to send reply with keyboard", e);
        }
    }
}
