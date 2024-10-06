package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.RequestUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DeclineCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public DeclineCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.decline")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        RequestUtil request = this.plugin.getRequestManager().getRequest(player);
        if (request == null) {
            player.sendMessage(messages.get("clan_decline_no_request", null));
            return true;
        }

        switch (request.getType()) {
            case CLAN_INVITE:
                handleClanInviteDecline(player, request);
                break;
            case PARTNERSHIP_INVITE:
                handlePartnershipDecline(player, request);
                break;
        }

        plugin.getRequestManager().clearRequest(player);

        return true;
    }

    private void handleClanInviteDecline(Player player, RequestUtil request) {
        String tag = request.getClan();
        player.sendMessage(messages.get("clan_decline_success", Map.of("clan", tag)));
    }

    private void handlePartnershipDecline(Player player, RequestUtil request) {
        String tag = request.getClan();
        player.sendMessage(messages.get("clan_partnership_decline_success", Map.of("clan", tag)));
        Player originator = request.getOriginator();
        originator.sendMessage(messages.get("clan_partnership_decline_originator", Map.of(
                "player", player.getName(),
                "clan", plugin.getClanManager().getClanByOfflinePlayer(player)
        )));
    }
}
