package com.zskv.heartToHeart;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class BroadcastCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
        if (args.length == 0) {
            sender.sendMessage(net.kyori.adventure.text.Component.text("Usage: /broadcast <message>", NamedTextColor.RED));
            return true;

        }

        String message = String.join(" ", args);

        Component broadcast = Component.text(message)
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);

        Bukkit.broadcast(broadcast);
        return true;
    }
}
