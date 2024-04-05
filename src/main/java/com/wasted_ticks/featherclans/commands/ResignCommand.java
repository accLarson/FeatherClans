package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

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
            return true;
        }

        if (!sender.hasPermission("feather.clans.resign")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_resign_error_no_clan", null));
            return true;
        }

        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(player);
        if (leader) {
            player.sendMessage(messages.get("clan_resign_error_leader", null));
            return true;
        }

        if (args.length == 1) {
            player.sendMessage(messages.get("clan_confirm_notice", Map.of(
                    "label", label,
                    "args", String.join(" ", args)
            )));
            return true;
        }
        else if (args.length == 2 && !args[1].equalsIgnoreCase("confirm")) {
            player.sendMessage(messages.get("clan_confirm_notice", Map.of(
                    "label", label,
                    "args", String.join(" ", Arrays.copyOf(args, args.length-1))
            )));
            return true;
        }
        else if (args.length >= 3) {
            player.sendMessage(messages.get("clan_resign_error_generic", null));
            return true;
        }

        boolean deleted = plugin.getClanManager().resignOfflinePlayer(player);
        if (!deleted) {
            player.sendMessage(messages.get("clan_resign_error_generic", null));
            return true;
        }

        player.sendMessage(messages.get("clan_resign_success", null));
        return true;

    }
}
