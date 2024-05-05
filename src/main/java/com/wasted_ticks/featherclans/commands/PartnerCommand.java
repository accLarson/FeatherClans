package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.managers.InviteRequestManager;
import com.wasted_ticks.featherclans.managers.PartnerRequestManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class PartnerCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final ClanManager manager;

    public PartnerCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.manager = plugin.getClanManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.partner")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        //check if own clan already has a partnership

        //check if own clan is elevated

        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_partner_request_error_no_clan_specified", null));
            return true;
        }

        String clan = args[1].toLowerCase();
        if (!manager.getClans().contains(clan)) {
            originator.sendMessage(messages.get("clan_partner_request_error_unresolved_clan", null));
            return true;
        }

        OfflinePlayer leader = Bukkit.getOfflinePlayer(manager.getLeader(clan));
        boolean isLeaderOnline = Bukkit.getOfflinePlayer(manager.getLeader(clan)).isOnline();
        if (!isLeaderOnline) {
            originator.sendMessage(messages.get("clan_partner_request_error_leader_offline", null));
            return true;
        }

        // check if requested partner already has a partner

        // check if requested partner is elevated


        plugin.getPartnerRequestManager().requestPartnership((Player) leader, clan, originator);
        return true;
    }
}
