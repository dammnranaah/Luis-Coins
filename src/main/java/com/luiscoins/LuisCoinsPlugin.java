package com.luiscoins;

import com.luiscoins.commands.BalanceCommand;
import com.luiscoins.commands.EarnCommand;
import com.luiscoins.commands.LuisAdminCommand;
import com.luiscoins.commands.LuisBalCommand;
import com.luiscoins.commands.PayCommand;
import com.luiscoins.listeners.JoinQuitListener;
import com.luiscoins.storage.BalanceStorage;
import com.luiscoins.storage.JsonBalanceStorage;
import com.luiscoins.storage.YamlBalanceStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LuisCoinsPlugin extends JavaPlugin {

    private BalanceStorage storage;
    private BalanceManager manager;
    private int autosaveTaskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupStorage();
        this.manager = new BalanceManager(this, storage);

        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("earn").setExecutor(new EarnCommand(this));
        getCommand("luis").setExecutor(new LuisAdminCommand(this));
        getCommand("luisbal").setExecutor(new LuisBalCommand(this));

        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(this), this);

        scheduleAutosave();

        getLogger().info("LuisCoins enabled.");
    }

    @Override
    public void onDisable() {
        if (autosaveTaskId != -1) Bukkit.getScheduler().cancelTask(autosaveTaskId);
        manager.saveAll();
        getLogger().info("LuisCoins saved and disabled.");
    }

    public void reload() {
        reloadConfig();
        setupStorage();
        manager.setStorage(storage);
        scheduleAutosave();
    }

    private void scheduleAutosave() {
        if (autosaveTaskId != -1) Bukkit.getScheduler().cancelTask(autosaveTaskId);
        int minutes = getConfig().getInt("autosave-minutes", 5);
        long ticks = Math.max(1, minutes) * 60L * 20L;
        autosaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, manager::saveAll, ticks, ticks);
    }

    private void setupStorage() {
        String mode = getConfig().getString("storage", "yaml").toLowerCase(Locale.ROOT);
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        if (mode.equals("json")) {
            storage = new JsonBalanceStorage(new File(dataFolder, "balances.json"));
        } else {
            storage = new YamlBalanceStorage(new File(dataFolder, "balances.yml"));
        }
    }

    public BalanceManager getManager() {
        return manager;
    }

    public String msg(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        return prefix + getConfig().getString("messages." + path, path);
    }
}
