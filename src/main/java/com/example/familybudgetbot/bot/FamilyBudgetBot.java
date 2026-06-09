package com.example.familybudgetbot.bot;

import com.example.familybudgetbot.bot.handler.CommandHandler;
import com.example.familybudgetbot.config.TelegramConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FamilyBudgetBot implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private final TelegramConfig telegramConfig;
    private final List<CommandHandler> handlers;

    @Override
    public String getBotToken() {
        return telegramConfig.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (!update.hasMessage()) {
                continue;
            }
            Message message = update.getMessage();
            if (!telegramConfig.getAllowedUserIds().contains(message.getFrom().getId())) {
                continue;
            }
            if (message.hasText()) {
                handlers.stream().filter(handler -> handler.supports(message.getText()))
                        .findFirst()
                        .ifPresent(handler -> {
                            try {
                                handler.handle(update);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
}
