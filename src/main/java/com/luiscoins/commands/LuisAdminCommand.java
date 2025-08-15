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
        if (args.length<1) { sender.sendMessage(color("&cUsage: /luis <add|remove|reset|set|reload>")); return true; }
        String sub=args[0].toLowerCase();
        if (sub.equals("reload")) {
            plugin.reload();
            sender.sendMessage(color(plugin.msg("reloaded")));
            return true;
        }
        if (args.length<2) { sender.sendMessage(color("&cUsage: /luis <add|remove|reset|set> <player> [amount]")); return true; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target==null || (target.getName()==null && !target.hasPlayedBefore())) { sender.sendMessage(color(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        if (sub.equals("reset")) {
            plugin.getManager().set(target.getUniqueId(), plugin.getConfig().getDouble("starting-balance",0));
            sender.sendMessage(color(plugin.msg("reset").replace("%player%", target.getName())));
            return true;
        }
        if (args.length<3) { sender.sendMessage(color("&cAmount required.")); return true; }
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
                sender.sendMessage(color("&cUnknown subcommand."));
                return true;
        }
    }

    private String color(String s){return s.replace('&','§');}
    private String format(double d){ return (Math.abs(d - Math.rint(d)) < 1e-9) ? String.valueOf((long)Math.rint(d)) : String.format("%.2f", d); }
}
