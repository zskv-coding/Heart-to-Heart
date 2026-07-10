package com.zskv.heartToHeart;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GameListener implements Listener {

    private final PlayerDataManager dataManager;

    public GameListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.get(player.getUniqueId());

        if (data.isAlive()) {
            HealthUtil.applyHearts(player, data.getHearts());
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = dataManager.get(player.getUniqueId());

        if (!data.isAlive()) {
            return;
        }

        Location deathLocation = player.getLocation();
        player.getWorld().strikeLightningEffect(deathLocation);

        // hearts doubles as the current life number, so dying on life 10 eliminates
        if (data.getHearts() >= PlayerDataManager.ELIMINATION_LIFE) {
            data.setAlive(false);
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            data.setHearts(data.getHearts() + 1);
        }

        dataManager.save();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.get(player.getUniqueId());

        if (!data.isAlive()) {
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }

        HealthUtil.applyHearts(player, data.getHearts());
        player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
    }
}

