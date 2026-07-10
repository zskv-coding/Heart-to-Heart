package com.zskv.heartToHeart;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class SessionManager {

    private static final int SESSION_SECONDS = 60 * 60;

    private final Plugin plugin;
    private final PlayerDataManager dataManager;

    private boolean active = false;
    private int secondsRemaining;
    private BukkitTask countdownTask;
    private BukkitTask footerTask;

    public SessionManager(Plugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        if (active) {
            return;
        }
        active = true;
        secondsRemaining = SESSION_SECONDS;

        World world = Bukkit.getWorlds().get(0);
        world.setTime(1000);
        world.setStorm(false);
        world.setThundering(false);
        world.setPVP(true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = dataManager.get(player.getUniqueId());
            if (data.isAlive()) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            player.sendTitle(" ", ChatColor.GREEN + "Session started.", 0, 40, 10);
        }

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickCountdown, 20L, 20L);
        footerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateFooter, 0L, 20L);
    }

    public void stop() {
        if (!active) {
            return;
        }
        active = false;

        Bukkit.getWorlds().get(0).setPVP(false);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = dataManager.get(player.getUniqueId());
            if (data.isAlive()) {
                player.setGameMode(GameMode.ADVENTURE);
            }
            player.sendTitle(" ", ChatColor.RED + "Session ended.", 0, 40, 10);
            player.sendPlayerListHeaderAndFooter(net.kyori.adventure.text.Component.empty(),
                    net.kyori.adventure.text.Component.empty());
        }

        if (countdownTask != null) {
            countdownTask.cancel();
        }

        if (footerTask != null) {
            footerTask.cancel();
        }
    }

    private void tickCountdown() {
        secondsRemaining--;
        if (secondsRemaining <= 0) {
            stop();
        }
    }

    private void updateFooter() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        String time = String.format("%02d:%02d", minutes, seconds);

        net.kyori.adventure.text.Component footer = net.kyori.adventure.text.Component.text(
                "Session ends in " +time, NamedTextColor.GRAY);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(net.kyori.adventure.text.Component.empty(), footer);
        }
    }
}

