package com.example.familybudgetbot.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final GoogleSheetsService googleSheetsService;
    @Getter
    private List<String> expenseCategories;
    @Getter
    private List<String> incomeCategories;
    private static final String EXPENSE_CATEGORIES_RANGE = "Категории!A2:A";
    private static final String INCOME_CATEGORIES_RANGE = "Категории!B2:B";

    private void readCategoriesFromSheets() {
        expenseCategories = googleSheetsService.readColumn(EXPENSE_CATEGORIES_RANGE);
        incomeCategories = googleSheetsService.readColumn(INCOME_CATEGORIES_RANGE);
    }

    public void reload() {
        readCategoriesFromSheets();
    }

    @PostConstruct
    public void init() {
        readCategoriesFromSheets();
    }
}
