package com.luiscoins.commands;

import com.luiscoins.LuisCoinsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public class LuisBalCommand implements CommandExecutor {
    private final LuisCoinsPlugin plugin;
    public LuisBalCommand(LuisCoinsPlugin plugin){this.plugin=plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length==1 && args[0].equalsIgnoreCase("top")) {
            if (!sender.hasPermission("luiscoins.top")) { sender.sendMessage(color("&cNo permission.")); return true; }
            sender.sendMessage(color(plugin.msg("top-header")));
            int rank=1;
            for (Map.Entry<UUID, Double> e : plugin.getManager().top(10)) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(e.getKey());
                String name = op!=null && op.getName()!=null? op.getName(): e.getKey().toString().substring(0,8);
                sender.sendMessage(color(plugin.msg("top-line").replace("%rank%", String.valueOf(rank++)).replace("%player%", name).replace("%amount%", format(e.getValue()))));
            }
            return true;
        }
        return Bukkit.dispatchCommand(sender, "balance" + (args.length>0? " "+args[0] : ""));
    }

    private String color(String s){return s.replace('&','§');}
    private String format(double d){ return (Math.abs(d - Math.rint(d)) < 1e-9) ? String.valueOf((long)Math.rint(d)) : String.format("%.2f", d); }
}
