package com.example.familybudgetbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramConfig {

    private String token;
    private String username;
    private List<Long> allowedUserIds;

}
