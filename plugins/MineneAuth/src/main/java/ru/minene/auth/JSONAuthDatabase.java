package ru.minene.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JSONAuthDatabase implements AuthDatabase {
    private final MineneAuth plugin;
    private File dataFile;
    private Map<UUID, PlayerData> players = new HashMap<>();
    private final Gson gson;
    
    public JSONAuthDatabase(MineneAuth plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }
    
    @Override
    public void initialize() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String fileName = plugin.getConfig().getString("database.file", "players.json");
        dataFile = new File(dataFolder, fileName);
        
        // Загрузка данных из файла
        loadData();
        
        plugin.getLogger().info("JSON база данных инициализирована!");
    }
    
    @Override
    public void close() {
        // Сохранение данных при закрытии
        saveData();
        plugin.getLogger().info("JSON база данных закрыта!");
    }
    
    private void loadData() {
        if (!dataFile.exists()) {
            plugin.getLogger().info("Файл данных не найден, будет создан новый.");
            players = new HashMap<>();
            return;
        }
        
        try (Reader reader = new InputStreamReader(
                Files.newInputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, PlayerData>>() {}.getType();
            Map<String, PlayerData> dataMap = gson.fromJson(reader, type);
            
            if (dataMap != null) {
                players.clear();
                for (Map.Entry<String, PlayerData> entry : dataMap.entrySet()) {
                    UUID uuid = UUID.fromString(entry.getKey());
                    players.put(uuid, entry.getValue());
                }
                plugin.getLogger().info("Загружено " + players.size() + " игроков из JSON файла.");
            } else {
                players = new HashMap<>();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при загрузке данных из JSON: " + e.getMessage());
            e.printStackTrace();
            players = new HashMap<>();
        }
    }
    
    private void saveData() {
        try {
            // Создание директории, если не существует
            File parentDir = dataFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Преобразование Map<UUID, PlayerData> в Map<String, PlayerData> для JSON
            Map<String, PlayerData> dataMap = new HashMap<>();
            for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
                dataMap.put(entry.getKey().toString(), entry.getValue());
            }
            
            // Сохранение в файл
            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
                gson.toJson(dataMap, writer);
            }
            
            plugin.getLogger().info("Данные сохранены в JSON файл.");
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при сохранении данных в JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean isPlayerRegistered(UUID uuid) {
        return players.containsKey(uuid);
    }
    
    @Override
    public void registerPlayer(UUID uuid, String password) {
        String hashedPassword = hashPassword(password);
        PlayerData data = new PlayerData(hashedPassword, System.currentTimeMillis());
        players.put(uuid, data);
        saveData(); // Сохраняем сразу после регистрации
        plugin.getLogger().info("Игрок " + uuid + " зарегистрирован.");
    }
    
    @Override
    public boolean verifyPassword(UUID uuid, String password) {
        PlayerData data = players.get(uuid);
        if (data == null) {
            return false;
        }
        return verifyPasswordHash(password, data.getPassword());
    }
    
    private String hashPassword(String password) {
        // Простое хеширование (в продакшене используйте BCrypt)
        return String.valueOf(password.hashCode());
    }
    
    private boolean verifyPasswordHash(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
    
    // Класс для хранения данных игрока
    private static class PlayerData {
        private String password;
        private long registeredAt;
        
        public PlayerData() {
            // Конструктор по умолчанию для Gson
        }
        
        public PlayerData(String password, long registeredAt) {
            this.password = password;
            this.registeredAt = registeredAt;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public long getRegisteredAt() {
            return registeredAt;
        }
        
        public void setRegisteredAt(long registeredAt) {
            this.registeredAt = registeredAt;
        }
    }
}

