package com.luiscoins;

import com.luiscoins.storage.BalanceStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BalanceManager {
    private final LuisCoinsPlugin plugin;
    private BalanceStorage storage;
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final Map<UUID, Long> earnCooldown = new ConcurrentHashMap<>();

    public BalanceManager(LuisCoinsPlugin plugin, BalanceStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        for (Player p : Bukkit.getOnlinePlayers()) loadPlayer(p.getUniqueId(), plugin.getConfig().getDouble("starting-balance", 0));
    }

    public void setStorage(BalanceStorage storage) {
        this.storage = storage;
    }

    public double get(UUID uuid) {
        return balances.getOrDefault(uuid, plugin.getConfig().getDouble("starting-balance", 0));
    }

    public void set(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
    }

    public void add(UUID uuid, double amount) {
        set(uuid, get(uuid) + Math.max(0, amount));
    }

    public boolean remove(UUID uuid, double amount) {
        if (amount < 0) amount = -amount;
        double cur = get(uuid);
        if (cur < amount) return false;
        set(uuid, cur - amount);
        return true;
    }

    public void loadPlayer(UUID uuid, double defaultBalance) {
        Double stored = storage.load(uuid);
        balances.put(uuid, stored != null ? stored : defaultBalance);
        Long cd = storage.loadCooldown(uuid);
        if (cd != null) earnCooldown.put(uuid, cd);
    }

    public void unloadPlayer(UUID uuid) {
        save(uuid);
        balances.remove(uuid);
        earnCooldown.remove(uuid);
    }

    public void save(UUID uuid) {
        storage.save(uuid, get(uuid));
        Long cd = earnCooldown.get(uuid);
        if (cd != null) storage.saveCooldown(uuid, cd);
    }

    public void saveAll() {
        for (UUID id : new ArrayList<>(balances.keySet())) save(id);
        storage.flush();
    }

    public List<Map.Entry<UUID, Double>> top(int n) {
        return balances.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .collect(Collectors.toList());
    }

    public long getEarnCooldownRemaining(UUID uuid) {
        long last = earnCooldown.getOrDefault(uuid, 0L);
        long cdMillis = plugin.getConfig().getInt("earn.cooldown-minutes", 0) * 60_000L;
        long remain = (last + cdMillis) - System.currentTimeMillis();
        return Math.max(0, remain);
    }

    public void markEarnUsed(UUID uuid) {
        earnCooldown.put(uuid, System.currentTimeMillis());
    }

    public double applyRankMultiplier(Player player, double base) {
        double result = base;
        if (plugin.getConfig().getConfigurationSection("rank-multipliers") == null) return result;
        Map<String, Object> map = plugin.getConfig().getConfigurationSection("rank-multipliers").getValues(false);
        double maxMult = 1.0;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String perm = e.getKey();
            double mult;
            try { mult = Double.parseDouble(String.valueOf(e.getValue())); } catch (Exception ex) { continue; }
            if (player.hasPermission(perm) && mult > maxMult) maxMult = mult;
        }
        result *= maxMult;
        return result;
    }
}
