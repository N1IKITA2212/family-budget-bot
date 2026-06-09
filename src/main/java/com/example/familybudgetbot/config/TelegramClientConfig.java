package com.example.familybudgetbot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@RequiredArgsConstructor
public class TelegramClientConfig {

    private final TelegramConfig telegramConfig;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(telegramConfig.getToken());
    }
}
