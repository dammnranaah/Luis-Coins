package com.luiscoins.listeners;

import com.luiscoins.LuisCoinsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {
    private final LuisCoinsPlugin plugin;
    public JoinQuitListener(LuisCoinsPlugin plugin){this.plugin=plugin;}

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getManager().loadPlayer(e.getPlayer().getUniqueId(), plugin.getConfig().getDouble("starting-balance", 0));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getManager().unloadPlayer(e.getPlayer().getUniqueId());
    }
}
