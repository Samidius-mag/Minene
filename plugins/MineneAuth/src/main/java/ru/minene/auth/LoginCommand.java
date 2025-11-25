package ru.minene.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
    private final MineneAuth plugin;
    
    public LoginCommand(MineneAuth plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.isAuthenticated(player.getUniqueId())) {
            player.sendMessage("§aВы уже авторизованы!");
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage("§cИспользование: /login <пароль>");
            return true;
        }
        
        String password = args[0];
        
        if (!plugin.getDatabase().isPlayerRegistered(player.getUniqueId())) {
            player.sendMessage("§cВы не зарегистрированы! Используйте /register");
            return true;
        }
        
        if (plugin.getDatabase().verifyPassword(player.getUniqueId(), password)) {
            plugin.authenticatePlayer(player.getUniqueId());
        } else {
            player.sendMessage("§cНеверный пароль!");
        }
        
        return true;
    }
}

