package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResignCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ResignCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(plugin.getClanManager().isOfflinePlayerInClan(player)) {
                boolean leader = plugin.getClanManager().isOfflinePlayerLeader(player);
                if(leader) {
                    player.sendMessage(messages.get("clan_resign_error_leader"));
                    return false;
                } else {
                    boolean deleted = plugin.getClanManager().resignOfflinePlayer(player);
                    if(deleted) {
                        player.sendMessage(messages.get("clan_resign_success"));
                        return true;
                    } else {
                        player.sendMessage(messages.get("clan_resign_error_generic"));
                        return false;
                    }
                }
            } else {
                player.sendMessage(messages.get("clan_resign_error_no_clan"));
                return false;
            }
        } else return false;
    }
}
