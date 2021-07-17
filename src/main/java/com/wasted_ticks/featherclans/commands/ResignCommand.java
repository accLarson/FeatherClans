package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ResignCommand implements CommandExecutor {

    private final FeatherClans plugin;

    public ResignCommand(FeatherClans plugin) {
        this.plugin  = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("ResignCommand");

        if(sender instanceof Player) {
            Player player = (Player) sender;
            ClanMember member = plugin.getClanManager().getClanMemberByPlayer(player);
            if(member != null) {

                boolean leader = plugin.getClanManager().isLeaderInClan(player);
                if(leader) {
                    player.sendMessage("Error: Unable to resign, as the leader you must disband the clan.");
                    return false;
                } else {
                    boolean deleted = plugin.getClanManager().deleteClanMember(member);
                    if(deleted) {
                        player.sendMessage("You've resigned from your clan.");
                        return true;
                    } else {
                        player.sendMessage("Error: Unable to resign.");
                        return false;
                    }
                }
            } else {
                player.sendMessage("Error: You are not currently in a clan.");
                return false;
            }
        } else return false;
    }
}
