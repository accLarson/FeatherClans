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

public class PromoteCommand implements CommandExecutor {


    private final FeatherClans plugin;
    private final FeatherClansMessages messages;


    public PromoteCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.promote")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }


        Player originator = (Player) sender;
        if (!plugin.getMembershipManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_promote_no_player", null));
            return true;
        }

        OfflinePlayer potentialOfficer = Bukkit.getOfflinePlayer(args[1]);

        if (!potentialOfficer.hasPlayedBefore()) {
            originator.sendMessage(messages.get("clan_promote_unresolved_player", null));
            return true;
        }

        if (potentialOfficer.equals(originator)) {
            sender.sendMessage(messages.get("clan_promote_error_leader", null));
            return true;
        }

        String clan = this.plugin.getMembershipManager().getClanByOfflinePlayer(originator);
        if (!this.plugin.getMembershipManager().isOfflinePlayerInSpecificClan(potentialOfficer, clan)) {
            originator.sendMessage(messages.get("clan_promote_not_in_clan", null));
            return true;
        }

        if (this.plugin.getMembershipManager().isOfflinePlayerOfficer(potentialOfficer)) {
            originator.sendMessage(messages.get("clan_promote_already_officer", null));
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
            originator.sendMessage(messages.get("clan_promote_error_generic", null));
            return true;
        }

        boolean successful = this.plugin.getMembershipManager().promoteOfficer(potentialOfficer);
        if (successful) {
            originator.sendMessage(messages.get("clan_promote_success_originator", Map.of(
                    "player", potentialOfficer.getName()
            )));
            if (potentialOfficer.isOnline()){
                ((Player)potentialOfficer).sendMessage(messages.get("clan_promote_success_player", Map.of(
                        "player", originator.getName(),
                        "clan", clan
                )));
            }
        } else {
            originator.sendMessage(messages.get("clan_promote_error_generic", null));
        }
        return true;

    }
}
