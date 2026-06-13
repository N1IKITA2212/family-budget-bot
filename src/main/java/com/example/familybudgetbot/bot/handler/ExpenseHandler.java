package com.example.familybudgetbot.bot.handler;

import com.example.familybudgetbot.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ExpenseHandler implements CommandHandler {
    private final CategoryService categoryService;
    private final TelegramClient telegramClient;
    private final SessionService sessionService;
    private final GoogleSheetsService googleSheetsService;

    private final MessageService messageService;

    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.hasCallbackQuery()
                ? update.getCallbackQuery().getFrom().getId()
                : update.getMessage().getFrom().getId();
        String firstName = update.hasCallbackQuery()
                ? update.getCallbackQuery().getFrom().getFirstName()
                : update.getMessage().getFrom().getFirstName();
        if (sessionService.getSession(userId).getState() == UserState.IDLE) {
            askCategoriesAndShowCategoriesButtons(update, userId);
            log.info("Запрошены категории трат и выведено меню с кнопками категорий");
            sessionService.updateState(userId, UserState.WAITING_EXPENSE_CATEGORY);
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_EXPENSE_CATEGORY) {
            getCategoryAndAskForExpenseAmount(update, userId);
            log.info("Пользователь выбрал категорию {}", update.getCallbackQuery().getData());
            sessionService.updateState(userId, UserState.WAITING_EXPENSE_AMOUNT);
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_EXPENSE_AMOUNT) {
            boolean success = getExpense(update);
            if (success) {
                messageService.askForComment(update);
                log.info("Пользователь ввел трату успешно и ему предложена возможность ввести комментарий");
                sessionService.updateState(userId, UserState.WAITING_EXPENSE_COMMENT);
            }
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_EXPENSE_COMMENT) {
            if (update.hasCallbackQuery() &&
                    update.getCallbackQuery().getData().equals("no_comment")) {
                log.info("Пользователь нажал на кнопку пропустить");
                saveExpense(userId, firstName, "", update);
                sessionService.updateComment(userId, "");
                messageService.sendMainMenu(getChatId(update), firstName);
            } else if (update.hasCallbackQuery() &&
                    update.getCallbackQuery().getData().equals("add_comment")) {
                log.info("Пользователь выбрал ввести комментарий");
                telegramClient.execute(SendMessage.builder()
                        .text("Введи комментарий")
                        .chatId(getChatId(update))
                        .build());
                sessionService.updateState(userId, UserState.WAITING_EXPENSE_COMMENT_INPUT);
            }
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_EXPENSE_COMMENT_INPUT) {
            String comment = update.getMessage().getText();
            saveExpense(userId, firstName, comment, update);
            messageService.sendMainMenu(getChatId(update), firstName);
        }
    }

    @Override
    public boolean supports(String command) {
        return command.equals("enter_expenses") || command.contains("expense_category");
    }

    @Override
    public boolean supportsState(UserState userState) {
        return userState == UserState.WAITING_EXPENSE_AMOUNT ||
                userState == UserState.WAITING_EXPENSE_COMMENT ||
                userState == UserState.WAITING_EXPENSE_COMMENT_INPUT;
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

    private void getCategoryAndAskForExpenseAmount(Update update, Long userId) throws TelegramApiException {
        String category = update.getCallbackQuery().getData().split(":")[1];

        SendMessage message = SendMessage.builder()
                .text("Введи сумму")
                .chatId(getChatId(update))
                .build();
        sessionService.updateCategory(userId, category);
        telegramClient.execute(message);
    }

    private boolean getExpense(Update update) throws TelegramApiException {
        try {
            BigDecimal expense = new BigDecimal(update.getMessage().getText());
            sessionService.updateAmount(update.getMessage().getFrom().getId(), expense);
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

    private void saveExpense(Long userId, String firstName, String comment, Update update) throws TelegramApiException {
        googleSheetsService.addExpense(
                sessionService.getSession(userId).getSelectedCategory(),
                sessionService.getSession(userId).getAmount(),
                firstName,
                comment
        );
        SendMessage message = SendMessage.builder()
                .text("✅ Трата записана!")
                .chatId(getChatId(update))
                .build();

        telegramClient.execute(message);
        sessionService.updateState(userId, UserState.IDLE);
    }
}
