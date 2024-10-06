package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ActivityManager;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.managers.RequestManager;
import com.wasted_ticks.featherclans.utilities.RequestUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PartnerCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final ClanManager manager;
    private final ActivityManager activityManager;

    public PartnerCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.manager = plugin.getClanManager();
        this.activityManager = plugin.getActivityManager();
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

        Player proposingLeader = (Player) sender;
        if (!manager.isOfflinePlayerLeader(proposingLeader)) {
            proposingLeader.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String proposingClan = plugin.getMembershipManager().getClanByOfflinePlayer(proposingLeader);

        if (manager.hasPartner(proposingClan)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_self_already_partnered", null));
            return true;
        }

        if (!activityManager.isClanActiveStatus(proposingClan)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_self_not_active_status", null));
            return true;
        }

        if (args.length != 2) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_no_clan_specified", null));
            return true;
        }

        String tag = args[1].toLowerCase();
        if (!manager.getClans().contains(tag)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_unresolved_clan", null));
            return true;
        }

        if (tag.equalsIgnoreCase(proposingClan)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_self_clan", null));
            return true;
        }

        if (manager.hasPartner(tag)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_already_partnered", null));
            return true;
        }

        OfflinePlayer receivingLeader = Bukkit.getOfflinePlayer(manager.getLeader(tag));
        boolean isLeaderOnline = Bukkit.getOfflinePlayer(manager.getLeader(tag)).isOnline();
        if (!isLeaderOnline) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_leader_offline", null));
            return true;
        }

        if (!activityManager.isClanActiveStatus(tag)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_not_active_status", null));
            return true;
        }

        double amount = this.plugin.getFeatherClansConfig().getEconomyPartnershipPrice();
        if (!plugin.getEconomy().has(proposingLeader, amount)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_economy", Map.of(
                    "amount", String.valueOf((int) amount)
            )));
            return true;
        }

        plugin.getRequestManager().createRequest(receivingLeader.getPlayer(), proposingClan, proposingLeader, RequestUtil.RequestType.PARTNERSHIP_INVITE);
        return true;
    }
}
