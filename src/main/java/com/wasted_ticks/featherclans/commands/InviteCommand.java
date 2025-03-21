package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.InviteManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class InviteCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public InviteCommand(FeatherClans plugin) {
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

        if (!originator.hasPermission("feather.clans.invite")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_invite_error_no_player_specified", null));
            return true;
        }

        Player invitee = Bukkit.getPlayer(args[1]);

        if (invitee == null) {
            originator.sendMessage(messages.get("clan_invite_error_unresolved_player", null));
            return true;
        }

        boolean inClan = plugin.getClanManager().isOfflinePlayerInClan(invitee);

        if (inClan) {
            originator.sendMessage(messages.get("clan_invite_error_already_in_clan", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);

        int max = this.plugin.getFeatherClansConfig().getClanMaxMembers();

        List<OfflinePlayer> players = plugin.getClanManager().getOfflinePlayersByClan(tag);

        if(players.size() >= max) {
            originator.sendMessage(messages.get("clan_invite_error_max", Map.of(
                    "max", String.valueOf(max)
            )));
            return true;
        }

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan invite " + invitee.getName())));
            return true;
        }

        InviteManager manager = plugin.getInviteManager();
        manager.invite(invitee, tag, originator);
        return true;
    }
}
