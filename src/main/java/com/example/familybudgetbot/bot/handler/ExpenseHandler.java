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
public class ExpenseHandler implements CommandHandler {
    private static final String SUPPORTS_DATA = "enter_expenses";
    private final CategoryService categoryService;
    private final TelegramClient telegramClient;
    private final SessionService sessionService;
    private static final List<UserState> supportsStates = List.of(UserState.WAITING_EXPENSE_AMOUNT);
    private final GoogleSheetsService googleSheetsService;


    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.hasCallbackQuery()
                ? update.getCallbackQuery().getFrom().getId()
                : update.getMessage().getFrom().getId();
        if (sessionService.getSession(userId).getState() == UserState.IDLE) {
            askCategoriesAndShowCategoriesButtons(update, userId);
            sessionService.updateState(userId, UserState.WAITING_EXPENSE_CATEGORY);
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_EXPENSE_CATEGORY) {
            String category = getCategoryAndAskForExpenseAmount(update, userId);
            sessionService.updateState(userId, UserState.WAITING_EXPENSE_AMOUNT);
            sessionService.updateCategory(userId, category);
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_EXPENSE_AMOUNT) {
            String category = sessionService.getSession(userId).getSelectedCategory();
            String username = update.getMessage().getFrom().getFirstName();
            boolean success = addExpenseToGoogleSheet(update, category, username);
            if (success) {
                sessionService.updateState(userId, UserState.IDLE);
            }
        }

    }

    @Override
    public boolean supports(String command) {
        return command.equals(SUPPORTS_DATA) || command.contains("expense_category");
    }

    @Override
    public boolean supportsState(UserState userState) {
        return supportsStates.contains(userState);
    }

    private void askCategoriesAndShowCategoriesButtons(Update update, Long userId) throws TelegramApiException {

        SendMessage message = SendMessage.builder()
                .text("Выбери категорию трат")
                .chatId(getChatId(update))
                .build();

        List<InlineKeyboardRow> rows = categoryService.getExpenseCategories().stream()
                .map(category -> InlineKeyboardButton.builder()
                        .text(category)
                        .callbackData("expense_category:" + category)
                        .build())
                .map(InlineKeyboardRow::new)
                .toList();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }

    private String getCategoryAndAskForExpenseAmount(Update update, Long userId) throws TelegramApiException {
        String category = update.getCallbackQuery().getData().split(":")[1];

        SendMessage message = SendMessage.builder()
                .text("Введи сумму")
                .chatId(getChatId(update))
                .build();
        telegramClient.execute(message);
        return category;
    }

    private boolean addExpenseToGoogleSheet(Update update, String category, String username) throws TelegramApiException {
        try {
            BigDecimal expense = new BigDecimal(update.getMessage().getText());
            googleSheetsService.addExpense(category, expense, username);
            SendMessage message = SendMessage.builder()
                    .text("✅ Трата записана!")
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
