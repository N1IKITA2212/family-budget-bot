package com.example.familybudgetbot.bot.handler;

import com.example.familybudgetbot.service.UserState;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandHandler {

    void handle(Update update) throws TelegramApiException;

    boolean supports(String command);

    boolean supportsState(UserState userState);
}
