package com.zskv.heartToHeart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.zskv.heartToHeart.SessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class H2HCommand implements CommandExecutor, TabCompleter {

    private final PlayerDataManager dataManager;
    private final SessionManager sessionManager;

    public H2HCommand(PlayerDataManager dataManager, SessionManager sessionManager) {
        this.dataManager = dataManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /h2h status <player|all> | /h2h hearts set <player> <amount>");
            return true;
        }

        if (args[0].equalsIgnoreCase("status") && args.length >= 2) {
            if (args[1].equalsIgnoreCase("all")) {
                sendAllStatus(sender);
            } else {
                sendPlayerStatus(sender, args[1]);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("seriesstart")) {
            new SeriesStartTask(dataManager).start();
            sender.sendMessage(ChatColor.GREEN + "Series start sequence initiated.");
            return true;
        }

        if (args[0].equalsIgnoreCase("hearts") && args.length >= 2 && args[1].equalsIgnoreCase("set")) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /h2h hearts set <player> <amount>");
                return true;
            }
            setHearts(sender, args[2], args[3]);
            return true;
        }

        if (args [0].equalsIgnoreCase("session") && args.length >=2) {
            if (args[1].equalsIgnoreCase("start")) {
                if (sessionManager.isActive()) {
                    sender.sendMessage(ChatColor.RED + "A session is already running");
                } else {
                    sessionManager.start();
                    sender.sendMessage(ChatColor.GREEN + "Session started.");
                }
            } else if (args[1].equalsIgnoreCase("stop")) {
                if (!sessionManager.isActive()) {
                    sender.sendMessage(ChatColor.RED + "No session is currently running.");
                } else {
                    sessionManager.stop();
                    sender.sendMessage(ChatColor.RED + "Session stopped.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /h2h session <start|stop>");
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /h2h status <player|all> | /h2h hearts set <player> <amount>");
        return true;
    }

    private void setHearts(CommandSender sender, String name, String amountArg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "That player has no Hearts To Hearts data.");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountArg);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a number.");
            return;
        }

        if (amount < 1) {
            sender.sendMessage(ChatColor.RED + "Amount must be at least 1.");
            return;
        }

        PlayerData data = dataManager.get(target.getUniqueId());
        data.setHearts(amount);

        boolean revived = amount < PlayerDataManager.ELIMINATION_LIFE && !data.isAlive();
        if (revived) {
            data.setAlive(true);
        }

        dataManager.save();

        if (target.isOnline()) {
            Player online = (Player) target;
            if (data.isAlive()) {
                online.setGameMode(GameMode.SURVIVAL);
                HealthUtil.applyHearts(online, amount);
            }
        }

        sender.sendMessage(ChatColor.GREEN + target.getName() + " now has " + amount + " hearts."
                + (revived ? ChatColor.GREEN + " They have been revived." : ""));
    }

    private void sendPlayerStatus(CommandSender sender, String name) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "That player has no Hearts To Hearts data.");
            return;
        }

        PlayerData data = dataManager.get(target.getUniqueId());
        sender.sendMessage(formatStatus(target.getName(), data));
    }

    private void sendAllStatus(CommandSender sender) {
        Map<UUID, PlayerData> all = dataManager.getAll();

        if (all.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No Hearts To Hearts data yet.");
            return;
        }

        for (Map.Entry<UUID, PlayerData> entry : all.entrySet()) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
            sender.sendMessage(formatStatus(target.getName(), entry.getValue()));
        }
    }

    private String formatStatus(String name, PlayerData data) {
        String status = data.isAlive() ? ChatColor.GREEN + "alive" : ChatColor.RED + "eliminated";

        return ChatColor.GRAY + name + ": " + ChatColor.WHITE + data.getHearts() + " hearts "
                + ChatColor.GRAY + "(" + status + ChatColor.GRAY + ")";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("status", "hearts", "seriesstart", "session"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            List<String> options = new ArrayList<>();
            options.add("all");
            options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            return filter(options, args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("hearts")) {
            return filter(List.of("set"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("hearts") && args[1].equalsIgnoreCase("set")) {
            List<String> options = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return filter(options, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("session")) {
            return filter(List.of( "start", "stop"), args[1]);
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}