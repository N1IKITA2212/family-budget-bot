package com.example.familybudgetbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final TelegramClient telegramClient;

    public void sendMainMenu(Long chatId, String firstName) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .text(firstName + ", что ты хочешь сделать?")
                .chatId(chatId)
                .build();

        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("💸 Внести траты")
                .callbackData("enter_expenses")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("🤑 Внести доходы")
                .callbackData("enter_income")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("📊 Статистика")
                .callbackData("show_stats")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }
}
