package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
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

        if(!(sender instanceof Player)){
            sender.sendMessage(messages.get("clan_error_player"));
            return false;
        }

        Player player = (Player) sender;
        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(player);
        if(!leader) {
            player.sendMessage(messages.get("clan_error_leader"));
            return false;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(player);
        Location location  = player.getLocation();
        boolean success = plugin.getClanManager().setClanHome(tag, location);
        if(!success) {
            player.sendMessage(messages.get("clan_sethome_error_generic"));
            return false;
        }

        player.sendMessage(messages.get("clan_sethome_success"));
        return true;

    }
}
