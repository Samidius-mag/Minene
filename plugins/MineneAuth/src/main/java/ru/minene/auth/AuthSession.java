package ru.minene.auth;

import java.util.UUID;

public class AuthSession {
    private UUID playerUUID;
    private boolean authenticated;
    private boolean isRegistered;
    
    public AuthSession(UUID playerUUID, boolean isRegistered) {
        this.playerUUID = playerUUID;
        this.authenticated = false;
        this.isRegistered = isRegistered;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public boolean isRegistered() {
        return isRegistered;
    }
}

