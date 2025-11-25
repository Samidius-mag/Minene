package ru.minene.rulers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class RulerCommand implements CommandExecutor {
    private final MineneRulers plugin;
    
    public RulerCommand(MineneRulers plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minenerulers.admin")) {
            sender.sendMessage("§cУ вас нет прав на выполнение этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§6=== MineneRulers ===");
            sender.sendMessage("§e/ruler spawn - создать всех правителей");
            sender.sendMessage("§e/ruler remove <state> - удалить правителя");
            sender.sendMessage("§e/ruler info - информация о правителях");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("spawn")) {
            plugin.spawnRulers();
            sender.sendMessage("§aВсе правители созданы!");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sender.sendMessage("§cИспользование: /ruler remove <omega|beta|vega>");
                return true;
            }
            
            String stateName = args[1].toLowerCase();
            plugin.removeRuler(stateName);
            sender.sendMessage("§aПравитель " + stateName + " удален!");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage("§6=== Информация о правителях ===");
            String[] states = {"omega", "beta", "vega"};
            for (String state : states) {
                Entity ruler = plugin.getRuler(state);
                if (ruler != null && ruler.isValid()) {
                    sender.sendMessage("§a" + state + ": §eсуществует");
                } else {
                    sender.sendMessage("§c" + state + ": §eне существует");
                }
            }
            return true;
        }
        
        sender.sendMessage("§cНеизвестная подкоманда!");
        return true;
    }
}

