package com.example.familybudgetbot.bot.handler;

import com.example.familybudgetbot.service.CategoryService;
import com.example.familybudgetbot.service.GoogleSheetsService;
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

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IncomeHandler implements CommandHandler {
    private final CategoryService categoryService;
    private final TelegramClient telegramClient;
    private final GoogleSheetsService googleSheetsService;
    private final SessionService sessionService;

    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.hasCallbackQuery()
                ? update.getCallbackQuery().getFrom().getId()
                : update.getMessage().getFrom().getId();
        if (sessionService.getSession(userId).getState() == UserState.IDLE) {
            askCategoriesAndShowCategoriesButtons(update);
            sessionService.updateState(userId, UserState.WAITING_INCOME_CATEGORY);
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_INCOME_CATEGORY) {
            String category = getCategoryAndAskForIncomeAmount(update);
            sessionService.updateState(userId, UserState.WAITING_INCOME_AMOUNT);
            sessionService.updateCategory(userId, category);
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_INCOME_AMOUNT) {
            String category = sessionService.getSession(userId).getSelectedCategory();
            String username = update.getMessage().getFrom().getFirstName();
            boolean success = addIncomeToGoogleSheet(update, category, username);
            if (success) {
                sessionService.updateState(userId, UserState.IDLE);
            }
        }
    }

    @Override
    public boolean supports(String command) {
        return command.equals("enter_income") || command.contains("income_category");
    }

    @Override
    public boolean supportsState(UserState userState) {
        return userState == UserState.WAITING_INCOME_AMOUNT;
    }

    private void askCategoriesAndShowCategoriesButtons(Update update) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .text("Выбери категорию доходов")
                .chatId(getChatId(update))
                .build();

        List<InlineKeyboardRow> rows = categoryService.getIncomeCategories().stream()
                .map(category -> InlineKeyboardButton.builder()
                        .text(category)
                        .callbackData("income_category:" + category)
                        .build())
                .map(InlineKeyboardRow::new)
                .toList();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }

    private String getCategoryAndAskForIncomeAmount(Update update) throws TelegramApiException {
        String category = update.getCallbackQuery().getData().split(":")[1];

        SendMessage message = SendMessage.builder()
                .text("Введи сумму")
                .chatId(getChatId(update))
                .build();

        telegramClient.execute(message);
        return category;
    }

    private boolean addIncomeToGoogleSheet(Update update, String category, String username) throws TelegramApiException {
        try {
            BigDecimal income = new BigDecimal(update.getMessage().getText());
            googleSheetsService.addIncome(category, income, username);
            SendMessage message = SendMessage.builder()
                    .text("✅ Доход записан!")
                    .chatId(getChatId(update))
                    .build();
            telegramClient.execute(message);
            return true;
        } catch (NumberFormatException e) {
            telegramClient.execute(SendMessage.builder()
                    .text("❌ Введи числовое значение, например: 1500")
                    .chatId(getChatId(update))
                    .build());
            return false;
        }
    }

    private Long getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return update.getMessage().getChatId();
    }
}
