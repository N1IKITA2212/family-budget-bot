package com.example.familybudgetbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramConfig {

    private String token;
    private String username;
    private List<Long> allowedUserIds;
}
