package ru.minene.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MineneLobby extends JavaPlugin implements Listener {
    
    private Location lobbyLocation;
    private PortalManager portalManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Загрузка координат лобби
        loadLobbyLocation();
        
        // Инициализация менеджера порталов
        portalManager = new PortalManager(this);
        portalManager.loadPortals();
        
        // Регистрация команд
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        
        // Регистрация событий
        getServer().getPluginManager().registerEvents(this, this);
        
        // Создание лобби при первом запуске
        new BukkitRunnable() {
            @Override
            public void run() {
                createLobby();
            }
        }.runTaskLater(this, 40L); // 2 секунды задержка для загрузки мира
        
        getLogger().info("MineneLobby включен!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MineneLobby выключен!");
    }
    
    private void loadLobbyLocation() {
        String worldName = getConfig().getString("lobby.world", "world");
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            getLogger().warning("Мир " + worldName + " не найден! Используется мир по умолчанию.");
            world = Bukkit.getWorlds().get(0);
        }
        
        double x = getConfig().getDouble("lobby.x", 0.5);
        double y = getConfig().getDouble("lobby.y", 100);
        double z = getConfig().getDouble("lobby.z", 0.5);
        float yaw = (float) getConfig().getDouble("lobby.yaw", 0);
        float pitch = (float) getConfig().getDouble("lobby.pitch", 0);
        
        lobbyLocation = new Location(world, x, y, z, yaw, pitch);
        
        // Установка точки спавна мира
        world.setSpawnLocation(lobbyLocation);
    }
    
    private void createLobby() {
        if (lobbyLocation == null) {
            getLogger().warning("Не удалось создать лобби: координаты не загружены!");
            return;
        }
        
        World world = lobbyLocation.getWorld();
        int size = getConfig().getInt("lobby-size", 100);
        int halfSize = size / 2;
        
        Material floorMaterial = Material.valueOf(getConfig().getString("floor-material", "QUARTZ_BLOCK"));
        Material wallMaterial = Material.valueOf(getConfig().getString("wall-material", "WHITE_CONCRETE"));
        
        int lobbyY = lobbyLocation.getBlockY() - 1;
        
        getLogger().info("Создание лобби размером " + size + "x" + size + "...");
        
        // Создание пола и стен
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Block block = world.getBlockAt(
                    lobbyLocation.getBlockX() + x,
                    lobbyY,
                    lobbyLocation.getBlockZ() + z
                );
                
                // Пол
                block.setType(floorMaterial);
                
                // Стены (только по периметру)
                if (x == -halfSize || x == halfSize || z == -halfSize || z == halfSize) {
                    for (int y = lobbyY + 1; y <= lobbyY + 5; y++) {
                        Block wallBlock = world.getBlockAt(
                            lobbyLocation.getBlockX() + x,
                            y,
                            lobbyLocation.getBlockZ() + z
                        );
                        wallBlock.setType(wallMaterial);
                    }
                }
            }
        }
        
        // Создание порталов
        portalManager.createPortals();
        
        getLogger().info("Лобби создано!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Проверка авторизации через MineneAuth
        if (Bukkit.getPluginManager().getPlugin("MineneAuth") != null) {
            // Телепортация будет выполнена после авторизации
            return;
        }
        
        // Если плагин авторизации не найден, телепортируем сразу
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lobbyLocation != null) {
                    player.teleport(lobbyLocation);
                }
            }
        }.runTaskLater(this, 20L);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Проверка входа в портал
        portalManager.checkPortalEntry(player, to);
    }
    
    public Location getLobbyLocation() {
        return lobbyLocation;
    }
    
    public void teleportToLobby(Player player) {
        if (lobbyLocation != null) {
            player.teleport(lobbyLocation);
            player.sendMessage("§aВы телепортированы в лобби!");
        } else {
            player.sendMessage("§cОшибка: Лобби не найдено!");
        }
    }
}

