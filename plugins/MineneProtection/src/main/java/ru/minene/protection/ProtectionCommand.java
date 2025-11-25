package ru.minene.protection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProtectionCommand implements CommandExecutor {
    private final MineneProtection plugin;
    
    public ProtectionCommand(MineneProtection plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6=== MineneProtection ===");
            sender.sendMessage("§e/protection info - информация о защите");
            if (sender.hasPermission("mineneprotection.admin")) {
                sender.sendMessage("§e/protection reload - перезагрузить конфигурацию");
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("info")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (plugin.isProtected(player.getLocation())) {
                    player.sendMessage("§cВы находитесь на защищенной территории!");
                } else {
                    player.sendMessage("§aВы находитесь на незащищенной территории");
                }
            } else {
                sender.sendMessage("Эта команда доступна только игрокам!");
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mineneprotection.admin")) {
                sender.sendMessage("§cУ вас нет прав на выполнение этой команды!");
                return true;
            }
            
            plugin.reloadProtection();
            sender.sendMessage("§aКонфигурация защиты перезагружена!");
            return true;
        }
        
        sender.sendMessage("§cНеизвестная подкоманда!");
        return true;
    }
}

