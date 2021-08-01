package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DisbandCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final ClanManager manager;
    private final FeatherClansMessages messages;

    public DisbandCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.manager = plugin.getClanManager();
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;
            boolean isLeader = manager.isLeaderInClan(player);
            ClanMember leader = manager.getClanMemberByPlayer(player);

            if(isLeader) {
                Clan clan = manager.getClanByClanMember(leader);
                List<ClanMember> members = manager.getClanMembersByClan(clan);
                for (ClanMember member: members) {
                    manager.deleteClanMember(member);
                }
                manager.deleteClan(clan);
                player.sendMessage(messages.get("clan_disband_success"));
                return true;
            } else {
                player.sendMessage(messages.get("clan_error_leader"));
                return false;
            }
        }
        return false;
    }
}
