package ru.minene.lobby;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PortalManager {
    private final MineneLobby plugin;
    private List<Portal> portals = new ArrayList<>();
    private Map<UUID, Long> portalCooldowns = new HashMap<>(); // Защита от повторных телепортаций
    private static final long PORTAL_COOLDOWN_MS = 2000; // 2 секунды cooldown
    
    public PortalManager(MineneLobby plugin) {
        this.plugin = plugin;
    }
    
    public void loadPortals() {
        ConfigurationSection portalsSection = plugin.getConfig().getConfigurationSection("portals");
        if (portalsSection == null) {
            plugin.getLogger().warning("Секция порталов не найдена в конфиге!");
            return;
        }
        
        for (String portalName : portalsSection.getKeys(false)) {
            ConfigurationSection portalConfig = portalsSection.getConfigurationSection(portalName);
            if (portalConfig != null) {
                Portal portal = loadPortal(portalName, portalConfig);
                if (portal != null) {
                    portals.add(portal);
                    plugin.getLogger().info("Портал " + portalName + " загружен");
                }
            }
        }
    }
    
    private Portal loadPortal(String name, ConfigurationSection config) {
        try {
            String worldName = config.getString("world", "world");
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("Мир " + worldName + " не найден для портала " + name);
                return null;
            }
            
            int x1 = config.getInt("x1");
            int y1 = config.getInt("y1");
            int z1 = config.getInt("z1");
            int x2 = config.getInt("x2");
            int y2 = config.getInt("y2");
            int z2 = config.getInt("z2");
            
            ConfigurationSection destConfig = config.getConfigurationSection("destination");
            if (destConfig == null) {
                plugin.getLogger().warning("Назначение портала " + name + " не найдено!");
                return null;
            }
            
            String destWorldName = destConfig.getString("world", "world");
            World destWorld = plugin.getServer().getWorld(destWorldName);
            if (destWorld == null) {
                destWorld = world;
            }
            
            double destX = destConfig.getDouble("x");
            double destY = destConfig.getDouble("y");
            double destZ = destConfig.getDouble("z");
            Location destination = new Location(destWorld, destX, destY, destZ);
            
            return new Portal(name, world, x1, y1, z1, x2, y2, z2, destination);
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при загрузке портала " + name + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public void createPortals() {
        Material portalMaterial = Material.valueOf(
            plugin.getConfig().getString("portal-material", "END_PORTAL_FRAME")
        );
        
        for (Portal portal : portals) {
            createPortal(portal, portalMaterial);
        }
    }
    
    private void createPortal(Portal portal, Material material) {
        World world = portal.getWorld();
        
        int minX = Math.min(portal.getX1(), portal.getX2());
        int maxX = Math.max(portal.getX1(), portal.getX2());
        int minY = Math.min(portal.getY1(), portal.getY2());
        int maxY = Math.max(portal.getY1(), portal.getY2());
        int minZ = Math.min(portal.getZ1(), portal.getZ2());
        int maxZ = Math.max(portal.getZ1(), portal.getZ2());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(material);
                }
            }
        }
        
        plugin.getLogger().info("Портал " + portal.getName() + " создан");
    }
    
    public void checkPortalEntry(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        
        // Проверка cooldown
        Long lastTeleport = portalCooldowns.get(playerId);
        if (lastTeleport != null && System.currentTimeMillis() - lastTeleport < PORTAL_COOLDOWN_MS) {
            return; // Игрок недавно телепортировался, пропускаем
        }
        
        for (Portal portal : portals) {
            if (portal.isInside(location)) {
                teleportPlayer(player, portal);
                break;
            }
        }
    }
    
    private void teleportPlayer(Player player, Portal portal) {
        Location destination = portal.getDestination();
        
        if (destination.getWorld() == null) {
            plugin.getLogger().warning("Мир назначения портала " + portal.getName() + " не найден!");
            return;
        }
        
        // Устанавливаем cooldown
        portalCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        
        player.teleport(destination);
        
        // Правильное название государства
        String stateName = portal.getName();
        if (stateName.equalsIgnoreCase("omega")) {
            stateName = "Омега";
        } else if (stateName.equalsIgnoreCase("beta")) {
            stateName = "Бета";
        } else if (stateName.equalsIgnoreCase("vega")) {
            stateName = "Вега";
        }
        
        player.sendMessage("§6Вы телепортированы в государство " + stateName + "!");
    }
    
    public boolean isLocationInPortal(Location location) {
        for (Portal portal : portals) {
            if (portal.isInside(location)) {
                return true;
            }
        }
        return false;
    }
    
    private static class Portal {
        private String name;
        private World world;
        private int x1, y1, z1;
        private int x2, y2, z2;
        private Location destination;
        
        public Portal(String name, World world, int x1, int y1, int z1, int x2, int y2, int z2, Location destination) {
            this.name = name;
            this.world = world;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.destination = destination;
        }
        
        public boolean isInside(Location location) {
            if (!location.getWorld().equals(world)) {
                return false;
            }
            
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);
            
            return x >= minX && x <= maxX &&
                   y >= minY && y <= maxY &&
                   z >= minZ && z <= maxZ;
        }
        
        public String getName() { return name; }
        public World getWorld() { return world; }
        public int getX1() { return x1; }
        public int getY1() { return y1; }
        public int getZ1() { return z1; }
        public int getX2() { return x2; }
        public int getY2() { return y2; }
        public int getZ2() { return z2; }
        public Location getDestination() { return destination; }
    }
}

