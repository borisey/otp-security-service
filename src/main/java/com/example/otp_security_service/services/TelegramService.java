package com.example.otp_security_service.services;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);

    private static final String TELEGRAM_API_URL_TEMPLATE = "https://api.telegram.org/bot%s/sendMessage";

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    public void sendCode(String destination, String code) {
        String message = String.format("%s, your confirmation code is: %s", destination, code);
        sendMessage(message);
    }

    public void sendMessage(String message) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            logger.warn("Telegram settings are missing. Message not sent.");
            return;
        }

        String encodedMessage = urlEncode(message);
        String url = String.format(TELEGRAM_API_URL_TEMPLATE, botToken) +
                "?chat_id=" + chatId +
                "&text=" + encodedMessage;

        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    logger.error("Telegram API error. Status code: {}", statusCode);
                } else {
                    logger.info("Telegram message sent successfully");
                }
            }
        } catch (IOException e) {
            logger.error("Error sending Telegram message: {}", e.getMessage());
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}