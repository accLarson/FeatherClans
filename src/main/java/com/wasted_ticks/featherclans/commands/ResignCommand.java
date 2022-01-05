package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResignCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ResignCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return false;
        }

        Player player = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_resign_error_no_clan", null));
            return false;
        }

        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(player);
        if (leader) {
            player.sendMessage(messages.get("clan_resign_error_leader", null));
            return false;
        }

        boolean deleted = plugin.getClanManager().resignOfflinePlayer(player);
        if (!deleted) {
            player.sendMessage(messages.get("clan_resign_error_generic", null));
            return false;
        }

        player.sendMessage(messages.get("clan_resign_success", null));
        return true;

    }
}
