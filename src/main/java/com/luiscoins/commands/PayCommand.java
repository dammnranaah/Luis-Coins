package com.luiscoins.commands;

import com.luiscoins.LuisCoinsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
    private final LuisCoinsPlugin plugin;
    public PayCommand(LuisCoinsPlugin plugin){this.plugin=plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
        if (!sender.hasPermission("luiscoins.pay")) { sender.sendMessage(color("&cNo permission.")); return true; }
        if (args.length!=2) { sender.sendMessage(color("&cUsage: /pay <player> <amount>")); return true; }
        Player p=(Player)sender;
        OfflinePlayer target=Bukkit.getOfflinePlayer(args[0]);
        if (target==null || (target.getName()==null && !target.hasPlayedBefore())) { sender.sendMessage(color(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        double amount;
        try{ amount=Double.parseDouble(args[1]); }catch(Exception e){ sender.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount"))); return true; }
        if (amount<=0) { sender.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount"))); return true; }
        if (!plugin.getManager().remove(p.getUniqueId(), amount)) { sender.sendMessage(color(plugin.getConfig().getString("messages.not-enough"))); return true; }
        plugin.getManager().add(target.getUniqueId(), amount);
        p.sendMessage(color(plugin.msg("paid").replace("%player%", target.getName()).replace("%amount%", format(amount))));
        if (target.isOnline()) {
            ((Player)target).sendMessage(color(plugin.msg("received").replace("%player%", p.getName()).replace("%amount%", format(amount))));
        }
        return true;
    }

    private String color(String s){return s.replace('&','§');}
    private String format(double d){ return (Math.abs(d - Math.rint(d)) < 1e-9) ? String.valueOf((long)Math.rint(d)) : String.format("%.2f", d); }
}
