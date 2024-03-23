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

public class PromoteCommand implements CommandExecutor {


    private final FeatherClans plugin;
    private final FeatherClansMessages messages;


    public PromoteCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.promote")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }


        Player originator = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_promote_no_player", null));
            return true;
        }

        Player potentialOfficer = Bukkit.getPlayer(args[1]);

        if (potentialOfficer == null) {
            originator.sendMessage(messages.get("clan_promote_unresolved_player", null));
            return true;
        }

        if (potentialOfficer.equals(originator)) {
            sender.sendMessage(messages.get("clan_promote_error_leader", null));
            return true;
        }

        String clan = this.plugin.getClanManager().getClanByOfflinePlayer(originator);
        if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(potentialOfficer, clan)) {
            originator.sendMessage(messages.get("clan_promote_not_in_clan", null));
            return true;
        }

        if (this.plugin.getClanManager().isOfflinePlayerOfficer(potentialOfficer)) {
            originator.sendMessage(messages.get("clan_promote_already_officer", null));
            return true;
        }

        boolean successful = this.plugin.getClanManager().promoteOfficer(potentialOfficer);
        if (successful) {
            originator.sendMessage(messages.get("clan_promote_success_originator", Map.of(
                    "player", potentialOfficer.getName()
            )));
            potentialOfficer.sendMessage(messages.get("clan_promote_success_player", Map.of(
                    "player", originator.getName(),
                    "clan", clan
            )));
        } else {
            originator.sendMessage(messages.get("clan_promote_error_generic", null));
        }
        return true;

    }
}
