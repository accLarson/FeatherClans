package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PartnerChatCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public PartnerChatCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.partnerchat")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;
        if (!plugin.getMembershipManager().isOfflinePlayerInClan(originator)) {
            originator.sendMessage(messages.get("clan_partnerchat_no_clan", null));
            return true;
        }

        String clan = plugin.getMembershipManager().getClanByOfflinePlayer(originator);
        if (!plugin.getClanManager().hasPartner(clan)) {
            originator.sendMessage(messages.get("clan_partnerchat_no_partner", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_partnerchat_no_message", null));
            return true;
        }

        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        String partnerClan = plugin.getClanManager().getPartner(clan);
        String[] clans = new String[]{clan, partnerClan};

        List<OfflinePlayer> clanPlayers = plugin.getMembershipManager().getOfflinePlayersByClan(clan);
        List<OfflinePlayer> partnerPlayers = plugin.getMembershipManager().getOfflinePlayersByClan(partnerClan);

        for (OfflinePlayer player : clanPlayers) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage(messages.get("clan_partnerchat_message_partner", Map.of(
                        "tag", clan,
                        "clan2", partnerClan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }

        for (OfflinePlayer player : partnerPlayers) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage(messages.get("clan_partnerchat_message", Map.of(
                        "tag", partnerClan,
                        "clan2", clan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }

        for (OfflinePlayer operator : plugin.getServer().getOperators()) {
            if (operator.isOnline()) {
                operator.getPlayer().sendMessage(messages.get("clan_partnerchat_spy_message", Map.of(
                        "clan1", clan,
                        "clan2", partnerClan,
                        "player", originator.getName(),
                        "message", message
                )));
            }
        }

        return true;
    }
}
