package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.managers.MembershipManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public class SeperateCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final ClanManager clanManager;
    private final MembershipManager membershipManager;

    public SeperateCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.clanManager = plugin.getClanManager();
        this.membershipManager = plugin.getMembershipManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission("feather.clans.seperate")) {
            player.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!membershipManager.isOfflinePlayerLeader(player)) {
            player.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String clanTag = membershipManager.getClanByOfflinePlayer(player);
        if (clanTag == null) {
            player.sendMessage(messages.get("clan_error_not_in_clan", null));
            return true;
        }

        if (!clanManager.hasPartner(clanTag)) {
            player.sendMessage(messages.get("clan_seperate_error_no_partner", null));
            return true;
        }

        if (args.length == 1) {
            player.sendMessage(messages.get("clan_confirm_notice", Map.of(
                    "label", label,
                    "args", String.join(" ", args)
            )));
            return true;
        } else if (args.length == 2 && !args[1].equalsIgnoreCase("confirm")) {
            player.sendMessage(messages.get("clan_confirm_notice", Map.of(
                    "label", label,
                    "args", String.join(" ", Arrays.copyOf(args, args.length-1))
            )));
            return true;
        } else if (args.length >= 3) {
            player.sendMessage(messages.get("clan_seperate_error_generic", null));
            return true;
        }

        String partnerTag = clanManager.getPartner(clanTag);
        boolean separated = clanManager.removePartnership(clanTag, partnerTag);

        if (!separated) {
            player.sendMessage(messages.get("clan_seperate_error_generic", null));
            return true;
        }

        player.sendMessage(messages.get("clan_seperate_success", Map.of(
                "partner", partnerTag
        )));

        return true;
    }
}
