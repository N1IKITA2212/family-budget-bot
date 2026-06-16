package com.example.familybudgetbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramConfig {

    private String token;
    private String username;
    private List<Long> allowedUserIds;

}
