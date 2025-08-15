package com.luiscoins.storage;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YamlBalanceStorage implements BalanceStorage {
    private final File file;
    private final YamlConfiguration cfg;

    public YamlBalanceStorage(File file) {
        this.file = file;
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public Double load(UUID uuid) {
        String path = "balances." + uuid.toString();
        if (cfg.contains(path)) return cfg.getDouble(path);
        return null;
    }

    @Override
    public void save(UUID uuid, double balance) {
        cfg.set("balances." + uuid.toString(), balance);
    }

    @Override
    public Long loadCooldown(UUID uuid) {
        String path = "cooldowns." + uuid.toString();
        if (cfg.contains(path)) return cfg.getLong(path);
        return null;
    }

    @Override
    public void saveCooldown(UUID uuid, long timestamp) {
        cfg.set("cooldowns." + uuid.toString(), timestamp);
    }

    @Override
    public void flush() {
        try { cfg.save(file); } catch (IOException ignored) {}
    }
}
