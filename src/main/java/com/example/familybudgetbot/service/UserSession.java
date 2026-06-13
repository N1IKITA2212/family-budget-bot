package com.example.familybudgetbot.service;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UserSession {
    private UserState state = UserState.IDLE;
    private String selectedCategory;
    private BigDecimal amount;
    private String comment;
}
