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

import java.util.Map;
import java.util.UUID;

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

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.kick")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!(plugin.getClanManager().isOfflinePlayerLeader(originator) || plugin.getClanManager().isOfflinePlayerOfficer(originator))) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_kick_error_no_player_specified", null));
            return true;
        }

        OfflinePlayer kickee = Bukkit.getOfflinePlayer(args[1]);

        String tag = this.plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(kickee, tag)) {

            if (this.plugin.getClanManager().isUsernameInSpecificClan(args[1],tag)) {
                UUID uuid = this.plugin.getClanManager().getUUIDFromUsername(args[1]);
                kickee = Bukkit.getOfflinePlayer(uuid);
            }
            else {
                originator.sendMessage(messages.get("clan_kick_error_not_in_clan", null));
                return true;
            }
        }

        if (this.plugin.getClanManager().isOfflinePlayerLeader(kickee)) {
            originator.sendMessage(messages.get("clan_kick_error_leader", null));
            return true;
        }

        if (this.plugin.getClanManager().isOfflinePlayerOfficer(kickee)) {
            originator.sendMessage(messages.get("clan_kick_error_officer", null));
            return true;
        }

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan kick " + kickee.getName())));
            return true;
        }

        boolean successful = this.plugin.getClanManager().resignOfflinePlayer(kickee);

        if (!successful) {
            originator.sendMessage(messages.get("clan_kick_error", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_kick_success", Map.of(
                "player", kickee.getName()
        )));

        if (kickee.isOnline()){
            kickee.getPlayer().sendMessage(messages.get("clan_kick_success_target", Map.of(
                    "clan", tag
            )));
        }

        plugin.getActiveManager().updateActiveStatus(kickee, tag);

        return true;
    }
}