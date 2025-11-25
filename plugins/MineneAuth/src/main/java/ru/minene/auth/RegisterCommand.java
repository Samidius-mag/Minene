package ru.minene.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
    private final MineneAuth plugin;
    
    public RegisterCommand(MineneAuth plugin) {
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
        
        if (plugin.getDatabase().isPlayerRegistered(player.getUniqueId())) {
            player.sendMessage("§cВы уже зарегистрированы! Используйте /login");
            return true;
        }
        
        if (args.length != 2) {
            player.sendMessage("§cИспользование: /register <пароль> <подтверждение пароля>");
            return true;
        }
        
        String password = args[0];
        String confirmPassword = args[1];
        
        int minLength = plugin.getConfig().getInt("min-password-length", 6);
        int maxLength = plugin.getConfig().getInt("max-password-length", 32);
        
        if (password.length() < minLength || password.length() > maxLength) {
            player.sendMessage("§cДлина пароля должна быть от " + minLength + " до " + maxLength + " символов!");
            return true;
        }
        
        if (!password.equals(confirmPassword)) {
            player.sendMessage("§cПароли не совпадают!");
            return true;
        }
        
        try {
            plugin.getDatabase().registerPlayer(player.getUniqueId(), password);
            player.sendMessage("§aРегистрация успешна!");
            plugin.authenticatePlayer(player.getUniqueId());
        } catch (Exception e) {
            player.sendMessage("§cОшибка при регистрации: " + e.getMessage());
            plugin.getLogger().severe("Ошибка регистрации: " + e.getMessage());
        }
        
        return true;
    }
}

