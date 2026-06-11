package com.example.familybudgetbot.bot.handler;

import com.example.familybudgetbot.service.MessageService;
import com.example.familybudgetbot.service.SessionService;
import com.example.familybudgetbot.service.StatsService;
import com.example.familybudgetbot.service.UserState;
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

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsHandler implements CommandHandler {
    private static final List<String> monthes = List.of(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    );
    private final TelegramClient telegramClient;
    private final StatsService statsService;
    private final SessionService sessionService;
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
            askMonthAndShowMonthButtons(update);
            sessionService.updateState(userId, UserState.WAITING_MONTH_FOR_STATS);
        } else if (sessionService.getSession(userId).getState() == UserState.WAITING_MONTH_FOR_STATS) {
            getMonthStatsAndSendToUser(update);
            sessionService.updateState(userId, UserState.IDLE);
            messageService.sendMainMenu(getChatId(update), firstName);
        }
    }

    @Override
    public boolean supports(String command) {
        return command.equals("show_stats") || command.contains("stats_month");
    }

    @Override
    public boolean supportsState(UserState userState) {
        return false;
    }

    private void askMonthAndShowMonthButtons(Update update) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .text("Выбери месяц, за который нужна статистика")
                .chatId(getChatId(update))
                .build();

        List<InlineKeyboardRow> rows = monthes.stream()
                .map(month -> InlineKeyboardButton.builder()
                        .text(month)
                        .callbackData("stats_month:" + month)
                        .build())
                .map(InlineKeyboardRow::new)
                .toList();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }

    private void getMonthStatsAndSendToUser(Update update) throws TelegramApiException {
        String month = update.getCallbackQuery().getData().split(":")[1];

        log.info("Selected month: '{}', index {}", month, monthes.indexOf(month));
        SendMessage message = SendMessage.builder()
                .text(statsService.getMonthlyStats(monthes.indexOf(month) + 1))
                .chatId(getChatId(update))
                .parseMode("Markdown")
                .build();

        telegramClient.execute(message);
    }

    private Long getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return update.getMessage().getChatId();
    }
}
