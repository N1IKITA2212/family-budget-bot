package com.example.familybudgetbot.bot.handler;

import com.example.familybudgetbot.service.MessageService;
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
    private final MessageService messageService;

    @Override
    public void handle(Update update) throws TelegramApiException {
        sessionService.getSession(update.getMessage().getFrom().getId()).setState(UserState.IDLE);
        SendMessage message = SendMessage.builder()
                .text("Привет, " + update.getMessage().getFrom().getFirstName() + "! 👋")
                .chatId(update.getMessage().getChatId())
                .build();
        telegramClient.execute(message);
        messageService.sendMainMenu(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName());
    }

    @Override
    public boolean supports(String command) {
        return SUPPORTS_COMMAND.equals(command);
    }

    @Override
    public boolean supportsState(UserState userState) {
        return false;
    }
}
