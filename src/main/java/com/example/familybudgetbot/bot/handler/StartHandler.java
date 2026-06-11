package com.example.familybudgetbot.bot.handler;

import com.example.familybudgetbot.service.SessionService;
import com.example.familybudgetbot.service.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartHandler implements CommandHandler {

    private static final String SUPPORTS_COMMAND = "/start";
    private final TelegramClient telegramClient;
    private final SessionService sessionService;
    private static final UserState SUPPORTS_STATE = UserState.IDLE;

    @Override
    public void handle(Update update) throws TelegramApiException {
        sessionService.getSession(update.getMessage().getFrom().getId());
        SendMessage message = SendMessage.builder()
                .text("Привет, " + update.getMessage().getFrom().getFirstName() + ", выбери, что ты хочешь сделать")
                .chatId(update.getMessage().getChatId())
                .build();


        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("💸 Внести траты")
                .callbackData("enter_expenses")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("🤑 Внести доходы")
                .callbackData("enter_income")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }

    @Override
    public boolean supports(String command) {
        return SUPPORTS_COMMAND.equals(command);
    }

    @Override
    public boolean supportsState(UserState userState) {
        return userState == SUPPORTS_STATE;
    }
}
