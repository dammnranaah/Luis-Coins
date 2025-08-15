package com.luiscoins.storage;

import java.util.UUID;

public interface BalanceStorage {
    Double load(UUID uuid);
    void save(UUID uuid, double balance);
    Long loadCooldown(UUID uuid);
    void saveCooldown(UUID uuid, long timestamp);
    void flush();
}
