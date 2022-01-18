package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RosterCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public RosterCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(messages.get("clan_roster_error_no_clan_specified", null));
            return true;
        }

        ClanManager manager = plugin.getClanManager();

        String input = args[1];
        if (!manager.getClans().stream().anyMatch(input::equalsIgnoreCase)) {
            sender.sendMessage(messages.get("clan_roster_error_unresolved_clan", null));
            return true;
        }

        sender.sendMessage(messages.get("clan_pre_line", null));
        List<OfflinePlayer> players = manager.getOfflinePlayersByClan(input.toLowerCase());
        sender.sendMessage(messages.get("clan_roster_members", Map.of(
                "clan", input.toLowerCase(),
                "count", String.valueOf(players.size())
        )));
        for (OfflinePlayer player : players) {
            String name = player.getName() != null ? player.getName() : "";
            if(manager.isOfflinePlayerLeader(player)) {
                sender.sendMessage(messages.get("clan_roster_player_leader", Map.of(
                        "player", name
                )));
            } else {
                sender.sendMessage(messages.get("clan_roster_player", Map.of(
                        "player", name
                )));
            }
        }
        sender.sendMessage(messages.get("clan_line", null));

        return true;
    }
}
