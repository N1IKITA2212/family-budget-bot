package com.example.familybudgetbot.bot.handler;

import com.example.familybudgetbot.service.CategoryService;
import com.example.familybudgetbot.service.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class ReloadHandler implements CommandHandler {
    private final CategoryService categoryService;
    private final TelegramClient telegramClient;

    @Override
    public void handle(Update update) throws TelegramApiException {
        categoryService.reload();

        telegramClient.execute(SendMessage.builder()
                .text("✅ Категории обновлены!")
                .chatId(update.getMessage().getChatId())
                .build());
    }

    @Override
    public boolean supports(String command) {
        return command.equals("/reload");
    }

    @Override
    public boolean supportsState(UserState userState) {
        return false;
    }
}
