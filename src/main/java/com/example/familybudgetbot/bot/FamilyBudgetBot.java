package com.example.familybudgetbot.bot;

import com.example.familybudgetbot.bot.handler.CommandHandler;
import com.example.familybudgetbot.config.TelegramConfig;
import com.example.familybudgetbot.service.SessionService;
import com.example.familybudgetbot.service.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FamilyBudgetBot implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private final TelegramConfig telegramConfig;
    private final List<CommandHandler> handlers;
    private final SessionService sessionService;

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
            if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                Long userId = update.getCallbackQuery().getFrom().getId();
                if (!telegramConfig.getAllowedUserIds().contains(userId)) {
                    continue;
                }
                handleUpdate(update, callbackData, userId);
            } else if (update.hasMessage()) {
                Message message = update.getMessage();
                Long userId = message.getFrom().getId();
                if (!telegramConfig.getAllowedUserIds().contains(userId)) {
                    continue;
                }
                if (message.hasText()) {
                    handleUpdate(update, message.getText(), userId);
                }
            }
        }
    }

    private void handleUpdate(Update update, String data, Long userId) {
        Optional<CommandHandler> handler = handlers.stream()
                .filter(h -> h.supports(data))
                .findFirst();

        if (handler.isEmpty()) {
            UserState userState = sessionService.getSession(userId).getState();
            handler = handlers.stream()
                    .filter(h -> h.supportsState(userState))
                    .findFirst();
        }
        handler.ifPresent(h -> {
            try {
                h.handle(update);
            } catch (TelegramApiException e) {
                log.error("Failed to handle update", e);
            }
        });
    }
}
