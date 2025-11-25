package ru.minene.protection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MineneProtection extends JavaPlugin implements Listener {
    
    private List<ProtectedArea> protectedAreas = new ArrayList<>();
    private List<LavaArea> lavaAreas = new ArrayList<>();
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadProtectedAreas();
        loadLavaAreas();
        
        getCommand("protection").setExecutor(new ProtectionCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("MineneProtection включен!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MineneProtection выключен!");
    }
    
    private void loadProtectedAreas() {
        protectedAreas.clear();
        
        // Загрузка защищенных территорий замков
        String[] states = {"omega", "beta", "vega"};
        
        for (String stateName : states) {
            String path = "states." + stateName;
            int centerX = getConfig().getInt(path + ".center-x");
            int centerZ = getConfig().getInt(path + ".center-z");
            int castleRadius = getConfig().getInt(path + ".castle-radius", 200);
            
            ProtectedArea area = new ProtectedArea(
                stateName + "_castle",
                centerX - castleRadius,
                centerZ - castleRadius,
                centerX + castleRadius,
                centerZ + castleRadius
            );
            
            protectedAreas.add(area);
            getLogger().info("Защищенная территория " + stateName + " загружена");
        }
    }
    
    private void loadLavaAreas() {
        lavaAreas.clear();
        
        int lavaWidth = getConfig().getInt("lava-width", 200);
        
        // Лава между Омегой и Бетой (север)
        lavaAreas.add(new LavaArea(0, 2500, 0, 2500 + lavaWidth));
        
        // Лава между Омегой и Вегой (юг)
        lavaAreas.add(new LavaArea(0, -2500 - lavaWidth, 0, -2500));
        
        // Лава между Бетой и Вегой (если нужно)
        // Можно добавить дополнительную логику
        
        getLogger().info("Территории лавы загружены");
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        if (player.hasPermission("mineneprotection.bypass")) {
            return;
        }
        
        Location loc = block.getLocation();
        
        // Проверка защищенных территорий
        if (isInProtectedArea(loc)) {
            event.setCancelled(true);
            player.sendMessage(getConfig().getString("messages.protected-territory", "§cЭта территория защищена!"));
            return;
        }
        
        // Проверка территории лавы
        if (isInLavaArea(loc)) {
            event.setCancelled(true);
            player.sendMessage(getConfig().getString("messages.lava-territory", "§cВы не можете строить в лаве между государствами!"));
            return;
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        if (player.hasPermission("mineneprotection.bypass")) {
            return;
        }
        
        Location loc = block.getLocation();
        
        // Проверка защищенных территорий
        if (isInProtectedArea(loc)) {
            event.setCancelled(true);
            player.sendMessage(getConfig().getString("messages.protected-territory", "§cЭта территория защищена!"));
            return;
        }
        
        // Проверка территории лавы
        if (isInLavaArea(loc)) {
            event.setCancelled(true);
            player.sendMessage(getConfig().getString("messages.lava-territory", "§cВы не можете строить в лаве между государствами!"));
            return;
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            
            if (player.hasPermission("mineneprotection.bypass")) {
                return;
            }
            
            // Проверка, не является ли цель правителем (будет проверено в плагине правителей)
            // Здесь только базовая проверка территории
        }
    }
    
    private boolean isInProtectedArea(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        
        for (ProtectedArea area : protectedAreas) {
            if (area.contains(x, z)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isInLavaArea(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        
        for (LavaArea area : lavaAreas) {
            if (area.contains(x, z)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void reloadProtection() {
        reloadConfig();
        loadProtectedAreas();
        loadLavaAreas();
        getLogger().info("Защита перезагружена!");
    }
    
    public boolean isProtected(Location loc) {
        return isInProtectedArea(loc) || isInLavaArea(loc);
    }
    
    private static class ProtectedArea {
        private String name;
        private int minX, minZ, maxX, maxZ;
        
        public ProtectedArea(String name, int minX, int minZ, int maxX, int maxZ) {
            this.name = name;
            this.minX = Math.min(minX, maxX);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxZ = Math.max(minZ, maxZ);
        }
        
        public boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
        
        public String getName() { return name; }
    }
    
    private static class LavaArea {
        private int minX, minZ, maxX, maxZ;
        
        public LavaArea(int minX, int minZ, int maxX, int maxZ) {
            this.minX = Math.min(minX, maxX);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxZ = Math.max(minZ, maxZ);
        }
        
        public boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }
}

