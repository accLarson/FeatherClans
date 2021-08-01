package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
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
    private final FeatherClansMessages messages;

    public SetHomeCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;
            boolean leader = plugin.getClanManager().isLeaderInClan(player);

            if(leader) {

                ClanMember member = plugin.getClanManager().getClanMemberByPlayer(player);
                Clan clan = plugin.getClanManager().getClanByClanMember(member);
                Location location  = player.getLocation();

                //check if player has sufficient balance.

                boolean isHomeSet = plugin.getClanManager().setClanHome(clan, location);

                if(isHomeSet) {
                    player.sendMessage(messages.get("clan_sethome_success"));
                    return true;
                } else {
                    player.sendMessage(messages.get("clan_sethome_error_generic"));
                    return false;
                }
            } else {
                player.sendMessage(messages.get("clan_error_leader"));
                return false;
            }
        } else return false;
    }
}
