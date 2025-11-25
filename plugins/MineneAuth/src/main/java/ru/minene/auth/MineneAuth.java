package ru.minene.auth;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineneAuth extends JavaPlugin implements Listener {
    
    private AuthDatabase database;
    private Map<UUID, AuthSession> sessions = new HashMap<>();
    private Map<UUID, BukkitRunnable> timeoutTasks = new HashMap<>();
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Инициализация базы данных (JSON)
        database = new JSONAuthDatabase(this);
        database.initialize();
        
        // Регистрация команд
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        
        // Регистрация событий
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("MineneAuth включен!");
    }
    
    @Override
    public void onDisable() {
        // Отмена всех задач таймаута
        timeoutTasks.values().forEach(BukkitRunnable::cancel);
        timeoutTasks.clear();
        
        // Закрытие базы данных
        if (database != null) {
            database.close();
        }
        
        getLogger().info("MineneAuth выключен!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Проверка, зарегистрирован ли игрок
        if (!database.isPlayerRegistered(uuid)) {
            // Игрок не зарегистрирован - требуется регистрация
            player.sendMessage("§6=== Добро пожаловать на сервер Minene! ===");
            player.sendMessage("§eДля начала игры необходимо зарегистрироваться.");
            player.sendMessage("§eИспользуйте команду: §a/register <пароль> <подтверждение>");
            player.sendMessage("§cВнимание: У вас есть 60 секунд на регистрацию!");
            
            AuthSession session = new AuthSession(uuid, false);
            sessions.put(uuid, session);
            startAuthTimeout(player);
        } else {
            // Игрок зарегистрирован - требуется вход
            player.sendMessage("§6=== Добро пожаловать обратно! ===");
            player.sendMessage("§eПожалуйста, войдите в систему.");
            player.sendMessage("§eИспользуйте команду: §a/login <пароль>");
            player.sendMessage("§cВнимание: У вас есть 60 секунд на вход!");
            
            AuthSession session = new AuthSession(uuid, true);
            sessions.put(uuid, session);
            startAuthTimeout(player);
        }
        
        // Заморозка игрока до авторизации
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        sessions.remove(uuid);
        
        BukkitRunnable task = timeoutTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isAuthenticated(player.getUniqueId())) {
            // Блокировка движения до авторизации
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // Разрешаем только команды авторизации
        if (!isAuthenticated(player.getUniqueId()) && 
            !command.startsWith("/login") && 
            !command.startsWith("/register") &&
            !command.startsWith("/l") &&
            !command.startsWith("/reg")) {
            event.setCancelled(true);
            player.sendMessage("§cСначала необходимо авторизоваться!");
        }
    }
    
    public boolean isAuthenticated(UUID uuid) {
        AuthSession session = sessions.get(uuid);
        return session != null && session.isAuthenticated();
    }
    
    public void authenticatePlayer(UUID uuid) {
        AuthSession session = sessions.get(uuid);
        if (session != null) {
            session.setAuthenticated(true);
            
            // Отмена таймаута
            BukkitRunnable task = timeoutTasks.remove(uuid);
            if (task != null) {
                task.cancel();
            }
            
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                // Восстановление скорости движения
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                
                player.sendMessage("§aВы успешно авторизованы!");
                player.sendMessage("§eТелепортируем вас в лобби...");
                
                // Телепортация в лобби
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Проверка наличия плагина лобби
                        JavaPlugin lobbyPlugin = (JavaPlugin) getServer().getPluginManager().getPlugin("MineneLobby");
                        if (lobbyPlugin != null && lobbyPlugin.isEnabled()) {
                            try {
                                // Используем метод teleportToLobby для безопасной телепортации
                                Object lobbyInstance = lobbyPlugin;
                                java.lang.reflect.Method teleportMethod = lobbyInstance.getClass().getMethod("teleportToLobby", Player.class);
                                teleportMethod.invoke(lobbyInstance, player);
                                return;
                            } catch (Exception e) {
                                getLogger().warning("Не удалось телепортировать в лобби: " + e.getMessage());
                                // Fallback: используем getLobbyLocation
                                try {
                                    java.lang.reflect.Method getLobbyMethod = lobbyPlugin.getClass().getMethod("getLobbyLocation");
                                    Location lobbyLoc = (Location) getLobbyMethod.invoke(lobbyPlugin);
                                    if (lobbyLoc != null) {
                                        player.teleport(lobbyLoc);
                                        return;
                                    }
                                } catch (Exception e2) {
                                    getLogger().warning("Не удалось получить координаты лобби: " + e2.getMessage());
                                }
                            }
                        }
                        // Fallback на точку спавна
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                }.runTaskLater(this, 20L); // 1 секунда задержка
            }
        }
    }
    
    public AuthDatabase getDatabase() {
        return database;
    }
    
    public int getAuthTimeout() {
        return getConfig().getInt("auth-timeout", 60);
    }
    
    private void startAuthTimeout(Player player) {
        UUID uuid = player.getUniqueId();
        int timeout = getAuthTimeout();
        
        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = timeout;
            
            @Override
            public void run() {
                if (!isAuthenticated(uuid)) {
                    if (timeLeft > 0 && timeLeft % 10 == 0) {
                        player.sendMessage("§cОсталось времени на авторизацию: " + timeLeft + " секунд");
                    }
                    timeLeft--;
                    
                    if (timeLeft <= 0) {
                        player.kickPlayer("§cВремя на авторизацию истекло!");
                        cancel();
                    }
                } else {
                    cancel();
                }
            }
        };
        
        task.runTaskTimer(this, 0L, 20L); // Каждую секунду
        timeoutTasks.put(uuid, task);
    }
}

