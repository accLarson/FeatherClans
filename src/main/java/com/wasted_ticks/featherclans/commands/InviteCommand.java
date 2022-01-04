package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.managers.InviteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InviteCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public InviteCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {

            Player originator = (Player) sender;
            boolean isLeader = plugin.getClanManager().isOfflinePlayerLeader(originator);

            if(isLeader) {

                if (args.length != 2) {
                    originator.sendMessage(messages.get("clan_invite_error_no_player_specified", null));
                    return false;
                }

                Player invitee = Bukkit.getPlayer(args[1]);

                if(invitee == null) {
                    originator.sendMessage(messages.get("clan_invite_error_unresolved_player", null));

                    return false;
                }

                boolean inClan = plugin.getClanManager().isOfflinePlayerInClan(invitee);
                if(inClan) {
                    originator.sendMessage(messages.get("clan_invite_error_already_in_clan", null));
                    return false;
                }

                String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);

                InviteManager manager = plugin.getInviteManager();
                manager.invite(invitee, tag, originator);

            } else {
                originator.sendMessage(messages.get("clan_error_leader", null));
                return false;
            }
        }
        return false;
    }
}
