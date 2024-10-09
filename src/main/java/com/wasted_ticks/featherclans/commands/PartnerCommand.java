package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ActivityManager;
import com.wasted_ticks.featherclans.managers.ClanManager;
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

        Player proposingLeader = (Player) sender;

        if (!sender.hasPermission("feather.clans.partner")) {
            proposingLeader.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_no_clan_specified", null));
            return true;
        }

        String tag = args[1].toLowerCase();

        if (args.length == 2 || !args[2].equalsIgnoreCase("confirm")) {
            sendConfirmationMessage(proposingLeader, tag);
            return true;
        }

        return handlePartnershipRequest(proposingLeader, tag);
    }

    private void sendConfirmationMessage(Player player, String targetClan) {
        double amount = this.plugin.getFeatherClansConfig().getEconomyPartnershipPrice();
        player.sendMessage(messages.get("clan_confirm_notice", Map.of(
                "label", "clan partner",
                "args", targetClan
        )));
        player.sendMessage(messages.get("clan_partner_request_text_economy", Map.of(
                "amount", String.valueOf((int) amount)
        )));
    }

    private boolean handlePartnershipRequest(Player proposingLeader, String tag) {
        String proposingClan = plugin.getMembershipManager().getClanByOfflinePlayer(proposingLeader);
        if (proposingClan == null) {
            proposingLeader.sendMessage(messages.get("clan_error_not_in_clan", null));
            return true;
        }

        if (!plugin.getMembershipManager().isOfflinePlayerLeader(proposingLeader)) {
            proposingLeader.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (manager.hasPartner(proposingClan)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_self_already_partnered", null));
            return true;
        }

        if (!activityManager.isClanActive(proposingClan)) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_self_not_active_status", null));
            return true;
        }

        if (tag == null || tag.isEmpty() || !manager.getClans().contains(tag)) {
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

        OfflinePlayer receivingLeader = Bukkit.getOfflinePlayer(plugin.getMembershipManager().getLeader(tag));
        if (!receivingLeader.isOnline()) {
            proposingLeader.sendMessage(messages.get("clan_partner_request_error_leader_offline", null));
            return true;
        }

        if (!activityManager.isClanActive(tag)) {
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

        boolean requestSent = plugin.getRequestManager().createRequest(receivingLeader.getPlayer(), proposingClan, proposingLeader, RequestUtil.RequestType.PARTNERSHIP_INVITE);
        if (!requestSent) proposingLeader.sendMessage(messages.get("clan_partner_request_error_already_sent_request", null));
        return true;
    }
}
