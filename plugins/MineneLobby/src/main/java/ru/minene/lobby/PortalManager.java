package ru.minene.lobby;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
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
        
        // Определяем направление портала (на какую стену он смотрит)
        BlockFace portalFace = determinePortalFace(minX, maxX, minZ, maxZ);
        
        // Создаем портал только в области портала (не заменяем всю стену)
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    // Заменяем блок только если это стена (не воздух и не пол/потолок)
                    if (block.getType() != Material.AIR) {
                        block.setType(material);
                    }
                }
            }
        }
        
        // Создаем табличку с названием портала
        createPortalSign(portal, portalFace);
        
        plugin.getLogger().info("Портал " + portal.getName() + " создан");
    }
    
    private BlockFace determinePortalFace(int minX, int maxX, int minZ, int maxZ) {
        // Определяем, на какую сторону смотрит портал
        // Портал Омеги: z = -25 (северная стена)
        // Портал Беты: z = 25 (южная стена)
        // Портал Веги: x = 25 (восточная стена)
        
        if (minZ == -25 || maxZ == -25) {
            return BlockFace.NORTH; // Северная стена
        } else if (minZ == 25 || maxZ == 25) {
            return BlockFace.SOUTH; // Южная стена
        } else if (minX == 25 || maxX == 25) {
            return BlockFace.EAST; // Восточная стена
        } else {
            return BlockFace.NORTH; // По умолчанию
        }
    }
    
    private void createPortalSign(Portal portal, BlockFace face) {
        World world = portal.getWorld();
        
        // Определяем название государства
        String stateName = portal.getName();
        if (stateName.equalsIgnoreCase("omega")) {
            stateName = "§6§lОмега";
        } else if (stateName.equalsIgnoreCase("beta")) {
            stateName = "§b§lБета";
        } else if (stateName.equalsIgnoreCase("vega")) {
            stateName = "§a§lВега";
        }
        
        // Координаты для таблички (перед порталом, на уровне середины)
        int centerX = (portal.getX1() + portal.getX2()) / 2;
        int centerY = (portal.getY1() + portal.getY2()) / 2;
        int centerZ = (portal.getZ1() + portal.getZ2()) / 2;
        
        // Размещаем табличку перед порталом
        int signX = centerX;
        int signY = centerY;
        int signZ = centerZ;
        
        switch (face) {
            case NORTH: // Портал смотрит на север, табличка на юг от портала
                signZ = centerZ + 1;
                break;
            case SOUTH: // Портал смотрит на юг, табличка на север от портала
                signZ = centerZ - 1;
                break;
            case EAST: // Портал смотрит на восток, табличка на запад от портала
                signX = centerX - 1;
                break;
            default:
                signZ = centerZ + 1;
        }
        
        Block signBlock = world.getBlockAt(signX, signY, signZ);
        signBlock.setType(Material.OAK_SIGN);
        
        // Устанавливаем текст на табличке
        if (signBlock.getState() instanceof org.bukkit.block.Sign) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signBlock.getState();
            
            // Устанавливаем направление таблички
            BlockData blockData = signBlock.getBlockData();
            if (blockData instanceof Directional) {
                Directional directional = (Directional) blockData;
                directional.setFacing(face.getOppositeFace()); // Табличка смотрит в сторону портала
                signBlock.setBlockData(blockData);
            }
            
            // Устанавливаем текст
            SignSide frontSide = sign.getSide(Side.FRONT);
            frontSide.setLine(0, "§7═══════════");
            frontSide.setLine(1, stateName);
            frontSide.setLine(2, "§7[Нажмите]");
            frontSide.setLine(3, "§7═══════════");
            
            sign.update();
        }
    }
    
    public void checkPortalEntry(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        
        // Проверка cooldown
        Long lastTeleport = portalCooldowns.get(playerId);
        if (lastTeleport != null && System.currentTimeMillis() - lastTeleport < PORTAL_COOLDOWN_MS) {
            return; // Игрок недавно телепортировался, пропускаем
        }
        
        // Проверяем точную позицию игрока
        Location playerLoc = player.getLocation();
        for (Portal portal : portals) {
            if (portal.isInside(location) || portal.isInside(playerLoc)) {
                teleportPlayer(player, portal);
                break;
            }
        }
    }
    
    public void showPortalHint(Player player, Location location) {
        // Показываем подсказку при приближении к порталу (в радиусе 3 блоков)
        for (Portal portal : portals) {
            double distance = portal.getDistanceToCenter(location);
            if (distance <= 3.0 && distance > 0.5) {
                String stateName = portal.getName();
                if (stateName.equalsIgnoreCase("omega")) {
                    stateName = "§6Омега";
                } else if (stateName.equalsIgnoreCase("beta")) {
                    stateName = "§bБета";
                } else if (stateName.equalsIgnoreCase("vega")) {
                    stateName = "§aВега";
                }
                player.sendActionBar("§7Подойдите ближе к порталу " + stateName + " §7или кликните по нему");
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
            
            // Проверяем точные координаты, а не только блок
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);
            
            // Проверяем, находится ли позиция внутри портала (с небольшим запасом)
            return x >= minX - 0.3 && x <= maxX + 0.3 &&
                   y >= minY - 0.3 && y <= maxY + 0.3 &&
                   z >= minZ - 0.3 && z <= maxZ + 0.3;
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
        
        public double getDistanceToCenter(Location location) {
            if (!location.getWorld().equals(world)) {
                return Double.MAX_VALUE;
            }
            
            double centerX = (x1 + x2) / 2.0;
            double centerY = (y1 + y2) / 2.0;
            double centerZ = (z1 + z2) / 2.0;
            
            double dx = location.getX() - centerX;
            double dy = location.getY() - centerY;
            double dz = location.getZ() - centerZ;
            
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
}

