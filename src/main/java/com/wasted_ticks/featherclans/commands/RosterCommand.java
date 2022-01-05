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

        UUID uuid = manager.getLeader(input);
        OfflinePlayer leader = Bukkit.getOfflinePlayer(uuid);
        String leaderName = leader.getName() != null ? leader.getName() : "";

        sender.sendMessage(messages.get("clan_pre_line", null));
        sender.sendMessage("");
        sender.sendMessage(messages.get("clan_roster_leader", Map.of(
                "clan", input.toLowerCase(),
                "leader", leaderName
        )));
        sender.sendMessage("");
        List<OfflinePlayer> players = manager.getOfflinePlayersByClan(input);
        sender.sendMessage(messages.get("clan_roster_members", Map.of(
                "count", String.valueOf(players.size())
        )));
        for (OfflinePlayer player : players) {
            String name = player.getName() != null ? player.getName() : "";
            sender.sendMessage(messages.get("clan_roster_player", Map.of(
                    "player", name
            )));
        }
        sender.sendMessage(messages.get("clan_line", null));

        return true;
    }
}
