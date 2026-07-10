package com.zskv.heartToHeart;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HeartToHeart extends JavaPlugin {

    private PlayerDataManager dataManager;
    private SessionManager sessionManager;

    @Override
    public void onEnable() {
        dataManager = new PlayerDataManager(this);
        sessionManager = new SessionManager(this, dataManager);
        getServer().getPluginManager().registerEvents(new GameListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new NoCommsListener(this), this);

        H2HCommand command = new H2HCommand(dataManager, sessionManager);
        Objects.requireNonNull(getCommand("h2h")).setExecutor(command);
        Objects.requireNonNull(getCommand("h2h")).setTabCompleter(command);
        Objects.requireNonNull(getCommand("broadcast")).setExecutor(new BroadcastCommand());
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
    }
}