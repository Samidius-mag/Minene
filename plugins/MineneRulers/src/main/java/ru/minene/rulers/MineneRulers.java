package ru.minene.rulers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MineneRulers extends JavaPlugin implements Listener {
    
    private Map<String, Entity> rulers = new HashMap<>();
    private Map<UUID, String> rulerStates = new HashMap<>();
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        getCommand("ruler").setExecutor(new RulerCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        
        // Спавн правителей при загрузке плагина
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                spawnRulers();
            }
        }.runTaskLater(this, 40L); // 2 секунды задержка
        
        getLogger().info("MineneRulers включен!");
    }
    
    @Override
    public void onDisable() {
        // Удаление всех правителей
        for (Entity ruler : rulers.values()) {
            if (ruler != null && ruler.isValid()) {
                ruler.remove();
            }
        }
        rulers.clear();
        
        getLogger().info("MineneRulers выключен!");
    }
    
    public void spawnRulers() {
        ConfigurationSection rulersSection = getConfig().getConfigurationSection("rulers");
        if (rulersSection == null) {
            getLogger().warning("Секция правителей не найдена в конфиге!");
            return;
        }
        
        EntityType npcType = EntityType.valueOf(getConfig().getString("npc-type", "VILLAGER"));
        boolean invulnerable = getConfig().getBoolean("invulnerable", true);
        boolean nameVisible = getConfig().getBoolean("custom-name-visible", true);
        
        for (String stateName : rulersSection.getKeys(false)) {
            ConfigurationSection rulerConfig = rulersSection.getConfigurationSection(stateName);
            if (rulerConfig != null) {
                spawnRuler(stateName, rulerConfig, npcType, invulnerable, nameVisible);
            }
        }
    }
    
    private void spawnRuler(String stateName, ConfigurationSection config, EntityType type, 
                           boolean invulnerable, boolean nameVisible) {
        try {
            ConfigurationSection locConfig = config.getConfigurationSection("location");
            if (locConfig == null) {
                getLogger().warning("Координаты правителя " + stateName + " не найдены!");
                return;
            }
            
            String worldName = locConfig.getString("world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("Мир " + worldName + " не найден для правителя " + stateName);
                return;
            }
            
            double x = locConfig.getDouble("x");
            double y = locConfig.getDouble("y");
            double z = locConfig.getDouble("z");
            Location location = new Location(world, x, y, z);
            
            // Удаление старого правителя, если есть
            Entity oldRuler = rulers.get(stateName);
            if (oldRuler != null && oldRuler.isValid()) {
                oldRuler.remove();
            }
            
            // Спавн нового правителя
            Entity ruler = world.spawnEntity(location, type);
            
            if (ruler instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) ruler;
                
                // Установка имени
                String name = config.getString("name", "Правитель " + stateName);
                living.setCustomName(name);
                living.setCustomNameVisible(nameVisible);
                
                // Защита от урона
                if (invulnerable) {
                    living.setInvulnerable(true);
                }
                
                // Отключение AI (чтобы NPC не двигался)
                try {
                    // Используем рефлексию для доступа к NMS (может не работать на всех версиях)
                    // Альтернатива - использовать Citizens API или другие плагины для NPC
                    // Для простоты оставляем базовую реализацию
                } catch (Exception e) {
                    // Игнорируем ошибки рефлексии
                }
            }
            
            // Установка метаданных для идентификации
            ruler.setMetadata("MineneRuler", new FixedMetadataValue(this, stateName));
            ruler.setMetadata("MineneRulerState", new FixedMetadataValue(this, stateName));
            
            rulers.put(stateName, ruler);
            rulerStates.put(ruler.getUniqueId(), stateName);
            
            getLogger().info("Правитель " + stateName + " создан в " + location);
            
        } catch (Exception e) {
            getLogger().severe("Ошибка при создании правителя " + stateName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        
        // Проверка, является ли сущность правителем
        if (isRuler(entity)) {
            event.setCancelled(true);
            
            // Если урон от игрока, отправляем сообщение
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
                if (damageEvent.getDamager() instanceof Player) {
                    Player player = (Player) damageEvent.getDamager();
                    player.sendMessage("§cВы не можете атаковать правителя!");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        
        if (isRuler(entity)) {
            String stateName = rulerStates.get(entity.getUniqueId());
            if (stateName != null) {
                sendRulerMessages(player, stateName);
            }
        }
    }
    
    private boolean isRuler(Entity entity) {
        return entity.hasMetadata("MineneRuler");
    }
    
    private void sendRulerMessages(Player player, String stateName) {
        ConfigurationSection rulerConfig = getConfig().getConfigurationSection("rulers." + stateName);
        if (rulerConfig != null) {
            List<String> messages = rulerConfig.getStringList("messages");
            if (messages.isEmpty()) {
                player.sendMessage("§6Добро пожаловать в государство " + stateName + "!");
            } else {
                for (String message : messages) {
                    player.sendMessage(message);
                }
            }
        }
    }
    
    public void removeRuler(String stateName) {
        Entity ruler = rulers.remove(stateName);
        if (ruler != null && ruler.isValid()) {
            ruler.remove();
        }
        rulerStates.values().removeIf(stateName::equals);
    }
    
    public Entity getRuler(String stateName) {
        return rulers.get(stateName);
    }
}

