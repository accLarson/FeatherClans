package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;


    public ChatCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.chat")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerInClan(originator)) {
            originator.sendMessage(messages.get("clan_chat_no_clan", null));
            return false;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_chat_no_message", null));
            return false;
        }

        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        String clan = plugin.getClanManager().getClanByOfflinePlayer(originator);

        List<OfflinePlayer> players = plugin.getClanManager().getOfflinePlayersByClan(clan);
        for (OfflinePlayer player : players) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage(messages.get("clan_chat_message", Map.of(
                        "tag", clan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }
        for (OfflinePlayer operator : plugin.getServer().getOperators()) {
            if (operator.isOnline()) {
                operator.getPlayer().sendMessage(messages.get("clan_chat_spy_message", Map.of(
                        "tag", clan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }

        return true;
    }
}
