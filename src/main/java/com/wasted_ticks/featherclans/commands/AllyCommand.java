package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Request;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class AllyCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final ClanManager manager;
    private final FeatherClansMessages messages;

    public AllyCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.manager = plugin.getClanManager();
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.ally")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!manager.isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String tag = manager.getClanByOfflinePlayer(originator);

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_ally_error_usage", null));
            return true;
        }

        String subCommand = args[1].toLowerCase();

        // Handle dissolve subcommand
        if (subCommand.equals("dissolve")) {
            if (args.length > 2 && !args[2].equalsIgnoreCase("confirm")) {
                originator.sendMessage(messages.get("clan_ally_error_usage", null));
                return true;
            }

            if (!manager.hasAlly(tag)) {
                originator.sendMessage(messages.get("clan_ally_error_no_alliance", null));
                return true;
            }

            String allyTag = manager.getAlly(tag.toLowerCase());

            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                originator.sendMessage(messages.get("clan_ally_dissolve_warning", Map.of("ally", allyTag)));
                originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan ally dissolve")));
                return true;
            }

            if (manager.removeAlliance(tag, allyTag)) {
                originator.sendMessage(messages.get("clan_ally_dissolve_success", Map.of("ally", allyTag)));

                // Notify the other clan's leader if online
                UUID allyLeaderUUID = manager.getLeader(allyTag);
                Player allyLeader = allyLeaderUUID != null ? Bukkit.getPlayer(allyLeaderUUID) : null;
                if (allyLeader != null && allyLeader.isOnline()) {
                    allyLeader.sendMessage(messages.get("clan_ally_dissolve_notification", Map.of("clan", tag)));
                }

                plugin.getDisplayManager().resetDisplays();
            } else {
                originator.sendMessage(messages.get("clan_ally_error_generic", null));
            }
            return true;
        }

        // Handle propose subcommand
        if (!subCommand.equals("propose")) {
            originator.sendMessage(messages.get("clan_ally_error_usage", null));
            return true;
        }

        if (args.length < 3) {
            originator.sendMessage(messages.get("clan_ally_error_no_clan_specified", null));
            return true;
        }

        String targetTag = args[2];

        // Check if originator's clan already has an ally BEFORE checking target
        if (manager.hasAlly(tag)) {
            originator.sendMessage(messages.get("clan_error_youre_already_allied", null));
            return true;
        }

        if (targetTag.equalsIgnoreCase(tag)) {
            originator.sendMessage(messages.get("clan_error_clan_doesnt_exist", null));
            return true;
        }

        if (!manager.getClans().contains(targetTag.toLowerCase())) {
            originator.sendMessage(messages.get("clan_error_clan_doesnt_exist", null));
            return true;
        }

        if (manager.hasAlly(targetTag)) {
            originator.sendMessage(messages.get("clan_error_theyre_already_allied", null));
            return true;
        }

        if (args.length < 4 || !args[3].equalsIgnoreCase("confirm")) {
            if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
                double amount = this.plugin.getFeatherClansConfig().getEconomyAlliancePrice();
                originator.sendMessage(messages.get("clan_economy_cost_warning_alliance_both", Map.of("amount", String.valueOf((int) amount))));
            }
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan ally propose " + targetTag.toLowerCase())));
            return true;
        }

        // Find target clan leader and ensure they're online
        UUID leaderUUID = manager.getLeader(targetTag.toLowerCase());
        Player targetPlayer = leaderUUID != null ? Bukkit.getPlayer(leaderUUID) : null;
        if (targetPlayer == null) {
            originator.sendMessage(messages.get("clan_invite_error_unresolved_player", null));
            return true;
        }

        // Send alliance request to target leader; include originator's clan tag as the request tag
        plugin.getInviteManager().addRequest(Request.RequestType.ALLIANCE, targetPlayer, originator, tag);

        return true;
    }
}
