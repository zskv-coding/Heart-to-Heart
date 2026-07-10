package com.zskv.heartToHeart;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    public static final int STARTING_HEARTS = 1;
    public static final int ELIMINATION_LIFE = 10;

    private final JavaPlugin plugin;
    private final File dataFile;
    private final Map<UUID, PlayerData> data = new HashMap<>();

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        load();
    }

    public PlayerData get(UUID uuid) {
        return data.computeIfAbsent(uuid, id -> new PlayerData(STARTING_HEARTS, true));
    }

    public Map<UUID, PlayerData> getAll() {
        return data;
    }

    private void load() {
        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int hearts = config.getInt(key + ".hearts", STARTING_HEARTS);
            boolean alive = config.getBoolean(key + ".alive", true);
            data.put(uuid, new PlayerData(hearts, alive));
        }
    }

    public void resetAllToOneHeart() {
        for (PlayerData playerData : data.values()) {
            playerData.setHearts(STARTING_HEARTS);
            playerData.setAlive(true);
        }
    }

    public void save() {
        plugin.getDataFolder().mkdirs();

        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> entry : data.entrySet()) {
            String key = entry.getKey().toString();
            config.set(key + ".hearts", entry.getValue().getHearts());
            config.set(key + ".alive", entry.getValue().isAlive());
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player data: " + e.getMessage());
        }

    }
}