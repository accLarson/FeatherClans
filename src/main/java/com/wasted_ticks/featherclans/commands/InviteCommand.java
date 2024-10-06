package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.RequestManager;
import com.wasted_ticks.featherclans.utilities.RequestUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class InviteCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public InviteCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.invite")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;
        if (!plugin.getMembershipManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_invite_error_no_player_specified", null));
            return true;
        }

        Player invitee = Bukkit.getPlayer(args[1]);
        if (invitee == null) {
            originator.sendMessage(messages.get("clan_invite_error_unresolved_player", null));
            return true;
        }

        boolean inClan = plugin.getMembershipManager().isOfflinePlayerInClan(invitee);
        if (inClan) {
            originator.sendMessage(messages.get("clan_invite_error_already_in_clan", null));
            return true;
        }

        String tag = plugin.getMembershipManager().getClanByOfflinePlayer(originator);
        int max = this.plugin.getFeatherClansConfig().getClanMaxMembers();
        List<OfflinePlayer> players = plugin.getMembershipManager().getOfflinePlayersByClan(tag);
        if(players.size() >= max) {
            originator.sendMessage(messages.get("clan_invite_error_max", Map.of(
                    "max", String.valueOf(max)
            )));
            return true;
        }

        RequestManager manager = plugin.getRequestManager();
        manager.createRequest(invitee, tag, originator, RequestUtil.RequestType.CLAN_INVITE);
        return true;
    }
}
