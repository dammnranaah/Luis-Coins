package com.luiscoins.commands;

import com.luiscoins.LuisCoinsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LuisAdminCommand implements CommandExecutor {
    private final LuisCoinsPlugin plugin;
    public LuisAdminCommand(LuisCoinsPlugin plugin){this.plugin=plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luiscoins.admin")) { sender.sendMessage(color("&cNo permission.")); return true; }
        if (args.length<1) {
            sendHelp(sender, label);
            return true;
        }
        String sub=args[0].toLowerCase();
        if (sub.equals("reload")) {
            plugin.reload();
            sender.sendMessage(color(plugin.msg("reloaded")));
            return true;
        }
        if (sub.equals("reset")) {
            if (args.length<2) { sender.sendMessage(color("&eUsage: /"+label+" reset <player>")); return true; }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target==null || (target.getName()==null && !target.hasPlayedBefore())) { sender.sendMessage(color(plugin.getConfig().getString("messages.player-not-found"))); return true; }
            plugin.getManager().set(target.getUniqueId(), plugin.getConfig().getDouble("starting-balance",0));
            sender.sendMessage(color(plugin.msg("reset").replace("%player%", target.getName())));
            return true;
        }

        if (!(sub.equals("add") || sub.equals("remove") || sub.equals("set"))) {
            sendHelp(sender, label);
            return true;
        }

        if (args.length<2) { sender.sendMessage(color("&eUsage: /"+label+" "+sub+" <player> <amount>")); return true; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target==null || (target.getName()==null && !target.hasPlayedBefore())) { sender.sendMessage(color(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        if (args.length<3) { sender.sendMessage(color("&eUsage: /"+label+" "+sub+" <player> <amount>")); return true; }
        double amount;
        try{ amount=Double.parseDouble(args[2]); }catch(Exception e){ sender.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount"))); return true; }
        switch (sub) {
            case "add":
                plugin.getManager().add(target.getUniqueId(), amount);
                sender.sendMessage(color(plugin.msg("added").replace("%player%", target.getName()).replace("%amount%", format(amount))));
                return true;
            case "remove":
                plugin.getManager().remove(target.getUniqueId(), amount);
                sender.sendMessage(color(plugin.msg("removed").replace("%player%", target.getName()).replace("%amount%", format(amount))));
                return true;
            case "set":
                plugin.getManager().set(target.getUniqueId(), amount);
                sender.sendMessage(color(plugin.msg("set").replace("%player%", target.getName()).replace("%amount%", format(amount))));
                return true;
            default:
                sendHelp(sender, label);
                return true;
        }
    }

    private String color(String s){return s.replace('&','§');}
    private String format(double d){ return (Math.abs(d - Math.rint(d)) < 1e-9) ? String.valueOf((long)Math.rint(d)) : String.format("%.2f", d); }
    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(color("&6LuisCoins Admin Commands:"));
        sender.sendMessage(color("&e/"+label+" add <player> <amount> &7- Add coins"));
        sender.sendMessage(color("&e/"+label+" remove <player> <amount> &7- Remove coins"));
        sender.sendMessage(color("&e/"+label+" set <player> <amount> &7- Set exact balance"));
        sender.sendMessage(color("&e/"+label+" reset <player> &7- Reset to starting-balance"));
        sender.sendMessage(color("&e/"+label+" reload &7- Reload config and data"));
    }
}
