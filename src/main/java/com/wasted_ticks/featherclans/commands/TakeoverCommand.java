package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.TimeUtility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TakeoverCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public TakeoverCommand(FeatherClans plugin) {
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

        if (!originator.hasPermission("feather.clans.takeover")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_takeover_error_leader", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerOfficer(originator)) {
            originator.sendMessage(messages.get("clan_error_officer", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);

        OfflinePlayer leader = Bukkit.getOfflinePlayer(plugin.getClanManager().getLeader(tag));

        int threshold = plugin.getFeatherClansConfig().getClanTakeoverDaysThreshold();
        
        int leaderDaysOffline = TimeUtility.getDaysSince(leader.getLastSeen());
        
        if (leaderDaysOffline <= threshold) {
            originator.sendMessage(messages.get("clan_takeover_error_threshold", Map.of(
                    "inactive_days", String.valueOf(leaderDaysOffline),
                    "threshold", String.valueOf(threshold))));
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_takeover_warning", Map.of("leader", leader.getName())));
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan takeover")));
            return true;
        }

        boolean success = plugin.getClanManager().setClanLeader(tag, originator);

        if (success) {
            this.plugin.getClanManager().setClanOfficerStatus(originator, false);
            this.plugin.getClanManager().setClanOfficerStatus(leader, true);
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan sethome")));

        } else originator.sendMessage(messages.get("clan_takeover_error_generic", null));

        return true;
    }
}
