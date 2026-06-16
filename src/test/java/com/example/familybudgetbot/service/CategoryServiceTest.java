package com.example.familybudgetbot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    GoogleSheetsService googleSheetsService;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void reloadCategories() {
        when(googleSheetsService.readColumn("Категории!A2:A"))
                .thenReturn(List.of("Продукты", "Транспорт"));

        categoryService.reload();

        assertEquals(List.of("Продукты", "Транспорт"), categoryService.getExpenseCategories());
    }

    @Test
    void reloadUpdatesBothCategories() {
        when(googleSheetsService.readColumn("Категории!A2:A"))
                .thenReturn(List.of("Продукты", "Транспорт"));
        when(googleSheetsService.readColumn("Категории!B2:B"))
                .thenReturn(List.of("Зарплата", "Фриланс"));

        categoryService.reload();

        assertEquals(List.of("Продукты", "Транспорт"), categoryService.getExpenseCategories());
        assertEquals(List.of("Зарплата", "Фриланс"), categoryService.getIncomeCategories());
    }
}
