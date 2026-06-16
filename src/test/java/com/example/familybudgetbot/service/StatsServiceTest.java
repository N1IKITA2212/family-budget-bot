package com.example.familybudgetbot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @Mock
    GoogleSheetsService googleSheetsService;
    @InjectMocks
    StatsService statsService;

    @Test
    void getMonthlyStatsTest() {

        when(googleSheetsService.readRange("06_Июнь!B2:D2"))
                .thenReturn(List.of(List.of("69000", "", "66788")));


        when(googleSheetsService.readRange("06_Июнь!A6:D17"))
                .thenReturn(List.of(
                        List.of("🛒 Продукты", "20000", "1500", "18500"),
                        List.of("🚌 Транспорт", "5000", "0 ₽", "5000")
                ));


        when(googleSheetsService.readRange("06_Июнь!B26:B27"))
                .thenReturn(List.of(List.of("73000"), List.of("71000")));

        String result = statsService.getMonthlyStats(6);
        assertTrue(result.contains("69000"));   // бюджет есть в строке
        assertTrue(result.contains("Продукты")); // категория есть
        assertFalse(result.contains("Транспорт")); // нулевая категория скрыта
    }
}
