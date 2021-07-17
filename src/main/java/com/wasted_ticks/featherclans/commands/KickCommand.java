package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KickCommand implements CommandExecutor {

    private final FeatherClans plugin;

    public KickCommand(FeatherClans plugin) {
        this.plugin  = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("KickCommand");
        if(sender instanceof Player) {

            Player player = (Player) sender;
            boolean leader = plugin.getClanManager().isLeaderInClan(player);

            if(leader) {

            } else {
                player.sendMessage("Error: You must be the clan leader to use this command.");
                return false;
            }
        }
        return false;
    }
}