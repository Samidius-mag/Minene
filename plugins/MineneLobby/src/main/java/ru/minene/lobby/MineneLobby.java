package ru.minene.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineneLobby extends JavaPlugin implements Listener {
    
    private Location lobbyLocation;
    private PortalManager portalManager;
    private Map<UUID, Long> teleportCooldowns = new HashMap<>(); // Защита от немедленной проверки порталов после телепортации
    private static final long TELEPORT_COOLDOWN_MS = 3000; // 3 секунды после телепортации
    
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
        
        // Периодическая проверка порталов для игроков, стоящих в них
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Проверяем только если игрок не на cooldown
                    UUID playerId = player.getUniqueId();
                    Long lastTeleport = teleportCooldowns.get(playerId);
                    if (lastTeleport != null && System.currentTimeMillis() - lastTeleport < TELEPORT_COOLDOWN_MS) {
                        continue;
                    }
                    
                    // Проверяем, находится ли игрок в портале
                    Location playerLoc = player.getLocation();
                    Location blockLoc = playerLoc.getBlock().getLocation();
                    portalManager.checkPortalEntry(player, blockLoc);
                }
            }
        }.runTaskTimer(this, 20L, 10L); // Каждые 0.5 секунды
        
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
        double baseY = getConfig().getDouble("lobby.y", 200);
        double z = getConfig().getDouble("lobby.z", 0.5);
        float yaw = (float) getConfig().getDouble("lobby.yaw", 0);
        float pitch = (float) getConfig().getDouble("lobby.pitch", 0);
        
        // Проверка и исправление старых координат (y=100 - это координаты правителя!)
        if (baseY < 150) {
            getLogger().warning("Обнаружены старые координаты лобби (y=" + baseY + ")! Исправляю на y=200");
            baseY = 200;
            // Обновляем конфиг
            getConfig().set("lobby.y", 200);
            saveConfig();
        }
        
        // Игрок появляется на 1 блок выше пола
        // Пол находится на baseY - 1, игрок на baseY
        double playerY = baseY;
        
        lobbyLocation = new Location(world, x, playerY, z, yaw, pitch);
        
        getLogger().info("Лобби локация загружена: " + lobbyLocation);
        
        // Установка точки спавна мира (только после создания лобби)
        // Не устанавливаем сразу, чтобы не конфликтовать с правителями
        // world.setSpawnLocation(lobbyLocation);
    }
    
    private void createLobby() {
        if (lobbyLocation == null) {
            getLogger().warning("Не удалось создать лобби: координаты не загружены!");
            return;
        }
        
        World world = lobbyLocation.getWorld();
        int size = getConfig().getInt("lobby-size", 50);
        int halfSize = size / 2;
        
        Material floorMaterial = Material.valueOf(getConfig().getString("floor-material", "WHITE_WOOL"));
        Material wallMaterial = Material.valueOf(getConfig().getString("wall-material", "WHITE_WOOL"));
        Material ceilingMaterial = Material.valueOf(getConfig().getString("ceiling-material", "WHITE_WOOL"));
        int roomHeight = getConfig().getInt("room-height", 20);
        
        // Пол находится на 1 блок ниже точки телепортации игрока
        int floorY = lobbyLocation.getBlockY() - 1;
        int ceilingY = floorY + roomHeight;
        
        getLogger().info("Создание лобби размером " + size + "x" + size + "x" + roomHeight + " из шерсти...");
        
        // Создание пола, стен и потолка
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                // Пол
                Block floorBlock = world.getBlockAt(
                    lobbyLocation.getBlockX() + x,
                    floorY,
                    lobbyLocation.getBlockZ() + z
                );
                floorBlock.setType(floorMaterial);
                
                // Потолок
                Block ceilingBlock = world.getBlockAt(
                    lobbyLocation.getBlockX() + x,
                    ceilingY,
                    lobbyLocation.getBlockZ() + z
                );
                ceilingBlock.setType(ceilingMaterial);
                
                // Стены (только по периметру)
                if (x == -halfSize || x == halfSize || z == -halfSize || z == halfSize) {
                    for (int y = floorY + 1; y <= ceilingY - 1; y++) {
                        Block wallBlock = world.getBlockAt(
                            lobbyLocation.getBlockX() + x,
                            y,
                            lobbyLocation.getBlockZ() + z
                        );
                        wallBlock.setType(wallMaterial);
                    }
                } else {
                    // Очистка внутреннего пространства (удаление блоков внутри комнаты)
                    for (int y = floorY + 1; y <= ceilingY - 1; y++) {
                        Block airBlock = world.getBlockAt(
                            lobbyLocation.getBlockX() + x,
                            y,
                            lobbyLocation.getBlockZ() + z
                        );
                        airBlock.setType(Material.AIR);
                    }
                }
            }
        }
        
        // Создание порталов (после создания стен, чтобы порталы были поверх них)
        portalManager.createPortals();
        
        // Установка точки спавна мира после создания лобби
        if (lobbyLocation != null) {
            lobbyLocation.getWorld().setSpawnLocation(lobbyLocation);
            getLogger().info("Точка спавна мира установлена на лобби: " + lobbyLocation);
        }
        
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Активация портала при клике
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                Location blockLoc = clickedBlock.getLocation();
                portalManager.checkPortalEntry(event.getPlayer(), blockLoc);
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Проверка cooldown после телепортации
        UUID playerId = player.getUniqueId();
        Long lastTeleport = teleportCooldowns.get(playerId);
        if (lastTeleport != null && System.currentTimeMillis() - lastTeleport < TELEPORT_COOLDOWN_MS) {
            return; // Игрок недавно был телепортирован, пропускаем проверку порталов
        }
        
        // Проверка входа в портал (проверяем точную позицию игрока)
        portalManager.checkPortalEntry(player, to);
        
        // Показываем подсказку при приближении к порталу
        portalManager.showPortalHint(player, to);
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Отключаем спавн мобов в лобби
        Location spawnLocation = event.getLocation();
        if (isInLobbyArea(spawnLocation)) {
            event.setCancelled(true);
        }
    }
    
    private boolean isInLobbyArea(Location location) {
        if (lobbyLocation == null) {
            return false;
        }
        
        if (!location.getWorld().equals(lobbyLocation.getWorld())) {
            return false;
        }
        
        int size = getConfig().getInt("lobby-size", 50);
        int halfSize = size / 2;
        
        int lobbyX = lobbyLocation.getBlockX();
        int lobbyZ = lobbyLocation.getBlockZ();
        
        int x = location.getBlockX();
        int z = location.getBlockZ();
        
        // Проверяем, находится ли локация в пределах лобби (с небольшим запасом)
        return Math.abs(x - lobbyX) <= halfSize + 5 && 
               Math.abs(z - lobbyZ) <= halfSize + 5;
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Устанавливаем cooldown при любой телепортации
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN || 
            event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            teleportCooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }
    
    public Location getLobbyLocation() {
        if (lobbyLocation == null) {
            return null;
        }
        
        // Возвращаем локацию, где игрок должен появиться (на 1 блок выше пола)
        // Локация уже настроена правильно при загрузке
        Location safeLocation = lobbyLocation.clone();
        
        // Убеждаемся, что локация безопасна для телепортации
        World world = safeLocation.getWorld();
        if (world != null) {
            int x = safeLocation.getBlockX();
            int y = safeLocation.getBlockY();
            int z = safeLocation.getBlockZ();
            
            // Проверяем, что точка телепортации не попадает в портал
            if (portalManager.isLocationInPortal(safeLocation)) {
                // Если попадает в портал, ищем безопасное место рядом
                int[] offsets = {1, 2, 3, -1, -2, -3};
                for (int offsetX : offsets) {
                    for (int offsetZ : offsets) {
                        Location testLocation = safeLocation.clone().add(offsetX, 0, offsetZ);
                        if (!portalManager.isLocationInPortal(testLocation)) {
                            safeLocation = testLocation;
                            x = safeLocation.getBlockX();
                            z = safeLocation.getBlockZ();
                            break;
                        }
                    }
                }
            }
            
            // Проверяем, что блоки не мешают (игрок на 1 блок выше пола)
            Block blockAt = world.getBlockAt(x, y, z);
            Block blockAbove = world.getBlockAt(x, y + 1, z);
            
            // Если блоки не воздух, ищем свободное место выше
            if (blockAt.getType() != Material.AIR || blockAbove.getType() != Material.AIR) {
                for (int checkY = y; checkY <= y + 3; checkY++) {
                    Block checkBlock = world.getBlockAt(x, checkY, z);
                    Block checkBlockAbove = world.getBlockAt(x, checkY + 1, z);
                    Location checkLoc = new Location(world, safeLocation.getX(), checkY, safeLocation.getZ());
                    if (checkBlock.getType() == Material.AIR && 
                        checkBlockAbove.getType() == Material.AIR &&
                        !portalManager.isLocationInPortal(checkLoc)) {
                        safeLocation.setY(checkY);
                        break;
                    }
                }
            }
        }
        
        return safeLocation;
    }
    
    public void teleportToLobby(Player player) {
        getLogger().info("teleportToLobby вызван для игрока " + player.getName());
        if (lobbyLocation != null) {
            getLogger().info("Лобби локация: " + lobbyLocation);
            // Создаем безопасную локацию для телепортации
            Location safeLocation = lobbyLocation.clone();
            
            // Убеждаемся, что игрок не застрянет в блоках
            World world = safeLocation.getWorld();
            int x = safeLocation.getBlockX();
            int y = safeLocation.getBlockY();
            int z = safeLocation.getBlockZ();
            
            // Проверяем, что точка телепортации не попадает в портал
            if (portalManager.isLocationInPortal(safeLocation)) {
                // Если попадает в портал, ищем безопасное место рядом
                int[] offsets = {1, 2, 3, -1, -2, -3, 4, -4};
                boolean found = false;
                for (int offsetX : offsets) {
                    for (int offsetZ : offsets) {
                        Location testLocation = safeLocation.clone().add(offsetX, 0, offsetZ);
                        if (!portalManager.isLocationInPortal(testLocation)) {
                            safeLocation = testLocation;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }
            
            // Проверяем блоки: под ногами должен быть пол (шерсть), на уровне игрока и выше - воздух
            Block blockBelow = world.getBlockAt(safeLocation.getBlockX(), safeLocation.getBlockY() - 1, safeLocation.getBlockZ());
            Block blockAt = world.getBlockAt(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ());
            Block blockAbove = world.getBlockAt(safeLocation.getBlockX(), safeLocation.getBlockY() + 1, safeLocation.getBlockZ());
            
            // Проверяем, что блок под ногами - это пол (шерсть), а не портал или воздух
            Material floorMaterial = Material.valueOf(getConfig().getString("floor-material", "WHITE_WOOL"));
            if (blockBelow.getType() != floorMaterial) {
                // Если под ногами не пол, ищем место где есть пол
                for (int checkY = safeLocation.getBlockY() - 2; checkY <= safeLocation.getBlockY() + 2; checkY++) {
                    Block checkBlockBelow = world.getBlockAt(safeLocation.getBlockX(), checkY - 1, safeLocation.getBlockZ());
                    Block checkBlockAt = world.getBlockAt(safeLocation.getBlockX(), checkY, safeLocation.getBlockZ());
                    Block checkBlockAbove = world.getBlockAt(safeLocation.getBlockX(), checkY + 1, safeLocation.getBlockZ());
                    if (checkBlockBelow.getType() == floorMaterial && 
                        checkBlockAt.getType() == Material.AIR && 
                        checkBlockAbove.getType() == Material.AIR &&
                        !portalManager.isLocationInPortal(new Location(world, safeLocation.getX(), checkY, safeLocation.getZ()))) {
                        safeLocation.setY(checkY);
                        break;
                    }
                }
            }
            
            // Дополнительная проверка: убеждаемся, что блоки на уровне игрока - воздух
            blockAt = world.getBlockAt(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ());
            blockAbove = world.getBlockAt(safeLocation.getBlockX(), safeLocation.getBlockY() + 1, safeLocation.getBlockZ());
            
            if (blockAt.getType() != Material.AIR || blockAbove.getType() != Material.AIR) {
                // Ищем свободное место выше
                for (int checkY = safeLocation.getBlockY(); checkY <= safeLocation.getBlockY() + 3; checkY++) {
                    Block checkBlock = world.getBlockAt(safeLocation.getBlockX(), checkY, safeLocation.getBlockZ());
                    Block checkBlockAbove = world.getBlockAt(safeLocation.getBlockX(), checkY + 1, safeLocation.getBlockZ());
                    Location checkLoc = new Location(world, safeLocation.getX(), checkY, safeLocation.getZ());
                    if (checkBlock.getType() == Material.AIR && 
                        checkBlockAbove.getType() == Material.AIR &&
                        !portalManager.isLocationInPortal(checkLoc)) {
                        safeLocation.setY(checkY);
                        break;
                    }
                }
            }
            
            // Устанавливаем cooldown перед телепортацией
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            
            getLogger().info("Телепортация игрока " + player.getName() + " в лобби на координаты: " + safeLocation);
            player.teleport(safeLocation);
            player.sendMessage("§aВы телепортированы в лобби!");
            getLogger().info("Игрок " + player.getName() + " телепортирован. Текущие координаты: " + player.getLocation());
        } else {
            getLogger().warning("Лобби локация не найдена для игрока " + player.getName() + "!");
            player.sendMessage("§cОшибка: Лобби не найдено!");
        }
    }
}

