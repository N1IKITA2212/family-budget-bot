package com.example.familybudgetbot.service;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession getSession(Long userId) {
        return sessions.computeIfAbsent(userId, id -> new UserSession());
    }

    public void updateState(Long userId, UserState state) {
        sessions.get(userId).setState(state);
    }

    public void updateCategory(Long userId, String category) {
        sessions.get(userId).setSelectedCategory(category);
    }
}
