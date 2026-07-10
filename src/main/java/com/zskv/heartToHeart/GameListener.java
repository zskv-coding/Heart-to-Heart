package com.zskv.heartToHeart;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GameListener implements Listener {

    private final PlayerDataManager dataManager;
    private final Map<UUID, String> pendingTitles = new HashMap<>();

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

        event.deathMessage(null);

        if (!data.isAlive()) {
            return;
        }

        Location deathLocation = player.getLocation();
        player.getWorld().strikeLightningEffect(deathLocation);

        Player killer = player.getKiller();
        if (killer != null) {
            PlayerData killerData = dataManager.get(killer.getUniqueId());
            if (killerData.isAlive() && killerData.getHearts() > 1) {
                killerData.setHearts(killerData.getHearts() - 1);
                HealthUtil.applyHearts(killer, killerData.getHearts());
                killer.sendTitle(" ", ChatColor.GREEN + "-1 heart", 0, 30, 10);
            }
        }

        // hearts doubles as the current life number, so dying on life 10 eliminates
        if (data.getHearts() >= PlayerDataManager.ELIMINATION_LIFE) {
            data.setAlive(false);
            player.setGameMode(GameMode.SPECTATOR);
            pendingTitles.put(player.getUniqueId(), ChatColor.RED + "ELIMINATED");

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
            }
            new EliminationBroadcastTask().start(dataManager.getPlugin());
        } else {
            data.setHearts(data.getHearts() + 1);
            pendingTitles.put(player.getUniqueId(), ChatColor.RED + "+1 heart");
        }

        dataManager.save();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.get(player.getUniqueId());

        if (!data.isAlive()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            HealthUtil.applyHearts(player, data.getHearts());
            player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
        }
        String subtitle = pendingTitles.remove(player.getUniqueId());
        if (subtitle != null) {
            player.sendTitle(" ", subtitle, 0, 30, 10);
        }
    }
}

