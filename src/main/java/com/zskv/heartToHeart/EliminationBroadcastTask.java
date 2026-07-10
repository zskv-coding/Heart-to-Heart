package com.zskv.heartToHeart;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EliminationBroadcastTask extends BukkitRunnable {

    private static final String FINAL_TEXT = "Someone has been eliminated.";
    private static final int DURATION_TICKS = 140; // 7 seconds
    private static final int UPDATE_INTERVAL = 2;

    private int elapsedTicks = 0;

    public void start(Plugin plugin) {
        runTaskTimer(plugin, 0L, UPDATE_INTERVAL);
    }

    @Override
    public void run() {
        if (elapsedTicks >= DURATION_TICKS) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendTitle(" ", ChatColor.RED + FINAL_TEXT, 0, 60, 20);
            }
            cancel();
            return;
        }

        int revealedCount = (int) ((double) elapsedTicks / DURATION_TICKS * FINAL_TEXT.length());

        String revealed = FINAL_TEXT.substring(0, revealedCount);
        String hidden = FINAL_TEXT.substring(revealedCount);

        String display = ChatColor.RED + revealed + ChatColor.RED + "" + ChatColor.MAGIC + hidden;

        broadcastTitle(display);
        elapsedTicks += UPDATE_INTERVAL;
    }

    private void broadcastTitle(String subtitle) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendTitle(" ", subtitle, 0, UPDATE_INTERVAL + 4,0);
        }
    }

}
