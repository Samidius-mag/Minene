package ru.minene.auth;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SQLiteAuthDatabase implements AuthDatabase {
    private final MineneAuth plugin;
    private Connection connection;
    
    public SQLiteAuthDatabase(MineneAuth plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File dbFile = new File(dataFolder, plugin.getConfig().getString("database.file", "players.db"));
        
        try {
            // Используем новый драйвер SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            // Создание таблицы
            String createTable = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL, " +
                    "registered_at INTEGER NOT NULL" +
                    ")";
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTable);
            }
            
            plugin.getLogger().info("База данных инициализирована!");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Ошибка при инициализации базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка при закрытии базы данных: " + e.getMessage());
            }
        }
    }
    
    @Override
    public boolean isPlayerRegistered(UUID uuid) {
        String sql = "SELECT uuid FROM players WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка при проверке регистрации: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void registerPlayer(UUID uuid, String password) {
        // Простое хеширование пароля (в продакшене используйте BCrypt)
        String hashedPassword = hashPassword(password);
        
        String sql = "INSERT INTO players (uuid, password, registered_at) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, hashedPassword);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка при регистрации игрока: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean verifyPassword(UUID uuid, String password) {
        String sql = "SELECT password FROM players WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return verifyPasswordHash(password, storedPassword);
            }
            
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка при проверке пароля: " + e.getMessage());
            return false;
        }
    }
    
    private String hashPassword(String password) {
        // Простое хеширование (в продакшене используйте BCrypt)
        // Для демонстрации используем простое хеширование
        return String.valueOf(password.hashCode());
    }
    
    private boolean verifyPasswordHash(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}

