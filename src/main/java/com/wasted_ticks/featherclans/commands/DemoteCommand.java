package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public class DemoteCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public DemoteCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.demote")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }


        Player originator = (Player) sender;
        if (!plugin.getMembershipManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_demote_no_player", null));
            return true;
        }

        OfflinePlayer potentialDemotedOfficer = Bukkit.getOfflinePlayer(args[1]);

        if (!potentialDemotedOfficer.hasPlayedBefore()) {
            originator.sendMessage(messages.get("clan_demote_unresolved_player", null));
            return true;
        }

        if (potentialDemotedOfficer.equals(originator)) {
            sender.sendMessage(messages.get("clan_demote_error_leader", null));
            return true;
        }

        String clan = this.plugin.getMembershipManager().getClanByOfflinePlayer(originator);
        if (!this.plugin.getMembershipManager().isOfflinePlayerInSpecificClan(potentialDemotedOfficer, clan)) {
            originator.sendMessage(messages.get("clan_demote_not_in_clan", null));
            return true;
        }

        if (!this.plugin.getMembershipManager().isOfflinePlayerOfficer(potentialDemotedOfficer)) {
            originator.sendMessage(messages.get("clan_demote_not_officer", null));
            return true;
        }

        if (args.length == 2) {
            originator.sendMessage(messages.get("clan_confirm_notice", Map.of(
                    "label", label,
                    "args", String.join(" ", args)
            )));
            return true;
        }
        else if (args.length == 3 && !args[2].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_confirm_notice", Map.of(
                    "label", label,
                    "args", String.join(" ", Arrays.copyOf(args, args.length-1))
            )));
            return true;
        }
        else if (args.length >= 4) {
            originator.sendMessage(messages.get("clan_demote_error_generic", null));
            return true;
        }

        boolean successful = this.plugin.getMembershipManager().demoteOfficer(potentialDemotedOfficer);
        if (successful) {
            originator.sendMessage(messages.get("clan_demote_success_originator", Map.of(
                    "player", potentialDemotedOfficer.getName()
            )));
            if (potentialDemotedOfficer.isOnline()) {
                ((Player)potentialDemotedOfficer).sendMessage(messages.get("clan_demote_success_player", Map.of(
                        "player", originator.getName(),
                        "clan", clan
                )));
            }
        } else {
            originator.sendMessage(messages.get("clan_demote_error_generic", null));
        }
        return true;
    }
}
