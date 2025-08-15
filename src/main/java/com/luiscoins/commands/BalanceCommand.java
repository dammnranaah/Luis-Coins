package com.luiscoins.commands;

import com.luiscoins.LuisCoinsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceCommand implements CommandExecutor {
    private final LuisCoinsPlugin plugin;
    public BalanceCommand(LuisCoinsPlugin plugin){this.plugin=plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length==0) {
            if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
            Player p=(Player)sender;
            double bal = plugin.getManager().get(p.getUniqueId());
            sender.sendMessage(color(plugin.msg("balance-self").replace("%amount%", format(bal))));
            return true;
        }
        if (!sender.hasPermission("luiscoins.balance.others")) { sender.sendMessage(color("&cNo permission.")); return true; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target==null || (target.getName()==null && !target.hasPlayedBefore())) { sender.sendMessage(color(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        double bal = plugin.getManager().get(target.getUniqueId());
        sender.sendMessage(color(plugin.msg("balance-other").replace("%player%", target.getName()).replace("%amount%", format(bal))));
        return true;
    }

    private String color(String s){return s.replace('&','§');}
    private String format(double d){ return (Math.abs(d - Math.rint(d)) < 1e-9) ? String.valueOf((long)Math.rint(d)) : String.format("%.2f", d); }
}
