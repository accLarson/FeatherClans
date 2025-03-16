package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.RequestUtility;
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
        RequestUtility request = this.plugin.getInviteManager().getRequest(player);
        if (request == null) {
            player.sendMessage(messages.get("clan_decline_no_invitation", null));
            return true;
        }

        String tag = request.getClan();
        player.sendMessage(messages.get("clan_decline_success", Map.of(
                "clan", tag
        )));

        Player originator = request.getOriginator();
        originator.sendMessage(messages.get("clan_decline_originator", Map.of(
                "player", player.getName()
        )));

        plugin.getInviteManager().clearRequest(player);

        return true;
    }
}
