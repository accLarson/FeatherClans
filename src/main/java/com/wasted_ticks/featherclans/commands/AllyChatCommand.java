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

public class AllyChatCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public AllyChatCommand(FeatherClans plugin) {
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
            originator.sendMessage(messages.get("clan_allychat_no_clan", null));
            return true;
        }

        String clan = plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (!plugin.getClanManager().hasAlly(clan)) {
            originator.sendMessage(messages.get("clan_allychat_no_ally", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_allychat_no_message", null));
            return true;
        }

        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        String allyClan = plugin.getClanManager().getAlly(clan.toLowerCase());

        // Send to own clan members
        List<OfflinePlayer> clanPlayers = plugin.getClanManager().getOfflinePlayersByClan(clan);
        for (OfflinePlayer player : clanPlayers) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage(messages.get("clan_allychat_message", Map.of(
                        "tag", clan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }

        // Send to ally clan members
        List<OfflinePlayer> allyPlayers = plugin.getClanManager().getOfflinePlayersByClan(allyClan);
        for (OfflinePlayer player : allyPlayers) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage(messages.get("clan_allychat_message", Map.of(
                        "tag", clan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }

        // Send to operators for spy
        for (OfflinePlayer operator : plugin.getServer().getOperators()) {
            if (operator.isOnline()) {
                operator.getPlayer().sendMessage(messages.get("clan_allychat_spy_message", Map.of(
                        "tag", clan,
                        "ally", allyClan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }

        return true;
    }
}
