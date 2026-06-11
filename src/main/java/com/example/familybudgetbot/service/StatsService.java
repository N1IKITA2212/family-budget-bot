package com.example.familybudgetbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {
    private final GoogleSheetsService googleSheetsService;

    public String getMonthlyStats(int monthNumber) {
        String month = getCurrentMonthSheet(monthNumber);
        String monthName = getCurrentMonthSheet(monthNumber).split("_")[1];
        log.info("Reading stats for sheet: '{}'", month);
        List<List<Object>> row1 = googleSheetsService.readRange(month + "!B2:D2");
        String budget = row1.get(0).get(0).toString();
        String remains = row1.get(0).get(2).toString();
        StringBuilder sb = new StringBuilder();
        sb.append("📊 Статистика за ").append("*").append(monthName).append(" ").append(LocalDate.now().getYear()).append("*").append("\n\n");
        sb.append("💼 Бюджет: ").append("_").append(budget).append("_").append(" \n");
        sb.append("💰 Остаток: ").append("_").append(remains).append("_").append(" \n");
        sb.append("\n💸 Расходы по категориям:\n");

        List<List<Object>> row2 = googleSheetsService.readRange(month + "!A6:D17");
        for (List<Object> row : row2) {
            if (row.isEmpty() || row.get(0).toString().contains("Итого")) break;

            String category = row.get(0).toString();
            String categoryBudget = row.get(1).toString();
            if (row.size() < 4) continue;
            String fact = row.get(2).toString();
            String categoryBalance = row.get(3).toString();

            if (!fact.equals("0 ₽")) {
                sb.append(category).append(" — ")
                        .append("_").append(fact).append("_").append(" из ")
                        .append("*").append(categoryBudget).append("*")
                        .append(" (остаток: ").append("_").append(categoryBalance).append("_").append(" )\n");
            }
        }
        sb.append("\n");

        List<List<Object>> totals = googleSheetsService.readRange(month + "!B26:B27");

        String income = totals.getFirst().getFirst().toString();
        String balance = totals.get(1).getFirst().toString();

        sb.append("💚 Доходы: ").append("_").append(income).append("_").append(" \n");
        sb.append("📊 Баланс: ").append("_").append(balance).append("_").append(" \n");
        return sb.toString();
    }

    private String getCurrentMonthSheet(int monthNumber) {
        return switch (monthNumber) {
            case 1 -> "01_Январь";
            case 2 -> "02_Февраль";
            case 3 -> "03_Март";
            case 4 -> "04_Апрель";
            case 5 -> "05_Май";
            case 6 -> "06_Июнь";
            case 7 -> "07_Июль";
            case 8 -> "08_Август";
            case 9 -> "09_Сентябрь";
            case 10 -> "10_Октябрь";
            case 11 -> "11_Ноябрь";
            case 12 -> "12_Декабрь";
            default -> throw new IllegalStateException("Неизвестный месяц");
        };
    }
}
