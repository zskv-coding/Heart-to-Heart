package com.zskv.heartToHeart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SeriesStartTask extends BukkitRunnable {

    private static final int BORDER_START_SIZE = 16;
    private static final int BORDER_END_SIZE = 2000;
    private static final int BORDER_EXPAND_SECONDS = 20;
    private static final int COUNTDOWN_SECONDS = 10;

    private final PlayerDataManager dataManager;
    private int secondsLeft = COUNTDOWN_SECONDS;

    public SeriesStartTask(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void start() {
        // every player is reset to 1 heart before the hype countdown begins
        for (Player player : Bukkit.getOnlinePlayers()) {
            dataManager.get(player.getUniqueId());
        }
        dataManager.resetAllToOneHeart();
        dataManager.save();

        WorldBorder border = Bukkit.getWorlds().get(0).getWorldBorder();
        border.setSize(BORDER_START_SIZE);
        border.changeSize(BORDER_END_SIZE, BORDER_EXPAND_SECONDS);

        for (Player player : Bukkit.getOnlinePlayers()) {
            HealthUtil.applyHearts(player, secondsLeft);
            player.setHealth(secondsLeft * 2.0);
        }

        runTaskTimer(dataManager.getPlugin(), 0L, 20L);
    }

    @Override
    public void run() {
        if (secondsLeft > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle(" ", ChatColor.RED + "" + secondsLeft, 0, 25, 5);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                HealthUtil.applyHearts(player, secondsLeft);
                player.setHealth(secondsLeft * 2.0);
            }
            secondsLeft--;
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = dataManager.get(player.getUniqueId());
                HealthUtil.applyHearts(player, data.getHearts());
                player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());

                player.sendTitle(" ", ChatColor.LIGHT_PURPLE + "Welcome to Heart 2 Heart!", 0, 30, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
            cancel();
        }
    }
}