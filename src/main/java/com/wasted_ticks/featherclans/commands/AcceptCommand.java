package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.util.RequestUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AcceptCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public AcceptCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.accept")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        boolean inClan = plugin.getClanManager().isOfflinePlayerLeader(player);
        if (inClan) {
            player.sendMessage(messages.get("clan_accept_in_clan", null));
            return false;
        }

        RequestUtil request = this.plugin.getInviteManager().getRequest(player);
        if (request == null) {
            player.sendMessage(messages.get("clan_accept_no_request", null));
            return false;
        }

        String tag = request.getClan();
        plugin.getClanManager().addOfflinePlayerToClan(player, tag);

        player.sendMessage(messages.get("clan_accept_success_player", Map.of(
                "clan", tag
        )));

        Player originator = request.getOriginator();
        originator.sendMessage(messages.get("clan_accept_success_originator", Map.of(
                "player", player.getName()
        )));

        plugin.getInviteManager().clearRequest(player);

        return true;
    }
}
