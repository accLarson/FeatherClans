package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetHomeCommand implements CommandExecutor {

    private final FeatherClans plugin;

    public SetHomeCommand(FeatherClans plugin) {
        this.plugin  = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("SetHomeCommand");
        if(sender instanceof Player) {

            Player player = (Player) sender;
            boolean leader = plugin.getClanManager().isLeaderInClan(player);

            if(leader) {

                ClanMember member = plugin.getClanManager().getClanMemberByPlayer(player);
                Clan clan = plugin.getClanManager().getClanByClanMember(member);
                Location location  = player.getLocation();

                //check if location is in banned world and refuse.

                //check if player has sufficient balance.

                boolean isHomeSet = plugin.getClanManager().setClanHome(clan, location);

                if(isHomeSet) {
                    player.sendMessage("You've set your clan home to your current location");
                    return true;
                } else {
                    player.sendMessage("Error: Unable to set clan home.");
                    return false;
                }
            } else {
                player.sendMessage("Error: You must be the clan leader to use this command.");
                return false;
            }
        } else return false;
    }
}
