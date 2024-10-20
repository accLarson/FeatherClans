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

public class KickCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public KickCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.kick")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;

        if (!plugin.getMembershipManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_kick_error_no_player_specified", null));
            return true;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

        if (!offlinePlayer.hasPlayedBefore()) {
            originator.sendMessage(messages.get("clan_kick_error_unresolved_player", null));
            return true;
        }

        String tag = this.plugin.getMembershipManager().getClanByOfflinePlayer(originator);
        if (!this.plugin.getMembershipManager().isOfflinePlayerInSpecificClan(offlinePlayer, tag)) {
            originator.sendMessage(messages.get("clan_kick_error_not_in_clan", null));
            return true;
        }

        if (this.plugin.getMembershipManager().isOfflinePlayerLeader(offlinePlayer)) {
            originator.sendMessage(messages.get("clan_kick_error_leader", null));
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
            originator.sendMessage(messages.get("clan_kick_error_generic", null));
            return true;
        }

        boolean successful = this.plugin.getMembershipManager().resignOfflinePlayer(offlinePlayer);
        if (!successful) {
            originator.sendMessage(messages.get("clan_kick_error", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_kick_success", Map.of(
                "player", offlinePlayer.getName()
        )));

        if (offlinePlayer.isOnline()){
            offlinePlayer.getPlayer().sendMessage(messages.get("clan_kick_success_target", Map.of(
                    "clan", tag
            )));
        }

        return true;
    }
}