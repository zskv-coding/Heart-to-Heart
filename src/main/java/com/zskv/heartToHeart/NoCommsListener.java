package com.zskv.heartToHeart;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class NoCommsListener implements Listener {

    private static final Set<String> BLOCKED_COMMANDS = Set.of(
            "me", "say", "msg", "tell", "w", "whisper"
    );

    private final Plugin plugin;

    public NoCommsListener(Plugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] parts = event.getMessage().substring(1).split (" ", 2);
        String label = parts[0].toLowerCase();

        int colonIndex = label.indexOf(':');
        if (colonIndex != -1) {
            label = label.substring(colonIndex + 1);

        }

        if (BLOCKED_COMMANDS.contains(label)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
                    player.sendMessage(net.kyori.adventure.text.Component.text("That command is disabled", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onChat (AsyncChatEvent event) {
        event.setCancelled(true);
        event.getPlayer().sendMessage(net.kyori.adventure.text.Component.text("shut up.", NamedTextColor.RED));
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        event.message(null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        Player joined = event.getPlayer();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(joined)) {
                continue;
            }
            joined.unlistPlayer(online);
            online.unlistPlayer(joined);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
    }
}
