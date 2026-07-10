package com.zskv.heartToHeart;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HeartToHeart extends JavaPlugin {

    private PlayerDataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new PlayerDataManager(this);

        getServer().getPluginManager().registerEvents(new GameListener(dataManager), this);

        H2HCommand command = new H2HCommand(dataManager);
        Objects.requireNonNull(getCommand("h2h")).setExecutor(command);
        Objects.requireNonNull(getCommand("h2h")).setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
    }
}