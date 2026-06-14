package com.example.familybudgetbot.service;

import com.example.familybudgetbot.config.TelegramConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    private final TelegramClient telegramClient;
    private final TelegramConfig telegramConfig;

    @Scheduled(cron = "0 0 19 * * *")
    public void eveningReminder() {

        telegramConfig.getAllowedUserIds()
                .forEach(id -> {
                    try {
                        telegramClient.execute(SendMessage.builder()
                                .text("💸 Не забудь записать траты за сегодня!")
                                .chatId(id)
                                .build());
                    } catch (TelegramApiException e) {
                        log.error("Failed to send reminder to user {}", id, e);
                    }
                });

    }
}
