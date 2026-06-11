package com.example.familybudgetbot.service;

import com.example.familybudgetbot.exception.GoogleSheetsException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsService {
    private final Sheets sheets;
    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;
    public static final String EXPENSES_SHEET = "📝 Журнал трат";
    public static final String INCOME_SHEET = "💚 Журнал доходов";
    private static final DateTimeFormatter DATE_FORMATTER =  DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private void appendToSheet(String sheetName, List<List<Object>> values) {
        AppendValuesResponse result = null;
        try {
            ValueRange body = new ValueRange().setValues(values);
            result = sheets.spreadsheets().values()
                    .append(spreadsheetId, sheetName, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            log.info("{} cells appended.", result.getUpdates().getUpdatedCells());
        } catch (IOException e) {
            throw new GoogleSheetsException(e.getMessage(), e.getCause());
        }
    }

    public void addExpense(String category, BigDecimal amount, String username) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        List<List<Object>> values = List.of(List.of(date, category, amount, username, ""));
        appendToSheet(EXPENSES_SHEET, values);
    }

    public void addIncome(String category, BigDecimal amount, String username) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        List<List<Object>> values = List.of(List.of(date, category, amount, username, ""));
        appendToSheet(INCOME_SHEET, values);
    }

    public List<String> readColumn(String range) {
        ValueRange result = null;
        try {
            result = sheets.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
        } catch (IOException e) {
            throw new GoogleSheetsException(e.getMessage(), e.getCause());
        }
        List<List<Object>> values = result.getValues();
        if (values == null) return List.of();

        return values.stream()
                .filter(row -> !row.isEmpty())
                .map(row -> row.getFirst().toString())
                .toList();
    }
}
