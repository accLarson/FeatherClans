package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DemoteCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public DemoteCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.demote")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }


        Player originator = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_demote_no_player", null));
            return true;
        }

        Player potentialDemotedOfficer = Bukkit.getPlayer(args[1]);

        if (potentialDemotedOfficer == null) {
            originator.sendMessage(messages.get("clan_demote_unresolved_player", null));
            return true;
        }

        if (potentialDemotedOfficer.equals(originator)) {
            sender.sendMessage(messages.get("clan_demote_error_leader", null));
            return true;
        }

        String clan = this.plugin.getClanManager().getClanByOfflinePlayer(originator);
        if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(potentialDemotedOfficer, clan)) {
            originator.sendMessage(messages.get("clan_demote_not_in_clan", null));
            return true;
        }

        if (!this.plugin.getClanManager().isOfflinePlayerOfficer(potentialDemotedOfficer)) {
            originator.sendMessage(messages.get("clan_demote_not_officer", null));
            return true;
        }

        boolean successful = this.plugin.getClanManager().demoteOfficer(potentialDemotedOfficer);
        if (successful) {
            originator.sendMessage(messages.get("clan_demote_success_originator", Map.of(
                    "player", potentialDemotedOfficer.getName()
            )));
            potentialDemotedOfficer.sendMessage(messages.get("clan_demote_success_player", Map.of(
                    "player", originator.getName(),
                    "clan", clan
            )));
        } else {
            originator.sendMessage(messages.get("clan_demote_error_generic", null));
        }
        return true;
    }
}
