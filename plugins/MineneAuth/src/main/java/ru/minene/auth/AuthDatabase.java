package ru.minene.auth;

import java.util.UUID;

public interface AuthDatabase {
    void initialize();
    void close();
    boolean isPlayerRegistered(UUID uuid);
    void registerPlayer(UUID uuid, String password);
    boolean verifyPassword(UUID uuid, String password);
}

