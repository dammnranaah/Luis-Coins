package com.luiscoins.commands;

import com.luiscoins.LuisCoinsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EarnCommand implements CommandExecutor {
    private final LuisCoinsPlugin plugin;
    public EarnCommand(LuisCoinsPlugin plugin){this.plugin=plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
        Player p = (Player) sender;
        if (!plugin.getConfig().getBoolean("earn.enabled", true)) { p.sendMessage(color("&cThis command is disabled.")); return true; }
        if (plugin.getConfig().getBoolean("earn.require-permission", false) && !p.hasPermission("luiscoins.earn")) {
            p.sendMessage(color("&cNo permission.")); return true; }
        if (args.length<1) { p.sendMessage(color("&cUsage: /earn <amount>")); return true; }
        double amount;
        try{ amount=Double.parseDouble(args[0]); }catch(Exception e){ p.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount"))); return true; }
        if (amount<=0) { p.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount"))); return true; }
        int max = plugin.getConfig().getInt("earn.max-per-use", 0);
        if (max>0 && amount>max) amount=max;
        long remain = plugin.getManager().getEarnCooldownRemaining(p.getUniqueId());
        if (remain>0) {
            long minutes = (remain + 59_999) / 60_000;
            p.sendMessage(color(plugin.msg("earn-cooldown").replace("%minutes%", String.valueOf(minutes))));
            return true;
        }
        amount = plugin.getManager().applyRankMultiplier(p, amount);
        plugin.getManager().add(p.getUniqueId(), amount);
        plugin.getManager().markEarnUsed(p.getUniqueId());
        p.sendMessage(color(plugin.msg("earned").replace("%amount%", format(amount))));
        return true;
    }

    private String color(String s){return s.replace('&','§');}
    private String format(double d){ return (Math.abs(d - Math.rint(d)) < 1e-9) ? String.valueOf((long)Math.rint(d)) : String.format("%.2f", d); }
}
