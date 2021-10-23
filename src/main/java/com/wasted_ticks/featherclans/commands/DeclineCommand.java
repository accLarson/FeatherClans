package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.util.Request;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeclineCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public DeclineCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player player = (Player) sender;
        boolean inClan = plugin.getClanManager().isPlayerInClan(player);
        if(inClan) {
            player.sendMessage("You are currently a member of a clan.");
            return false;
        }

        Request request = this.plugin.getInviteManager().getRequest(player);

        if(request == null) {
            player.sendMessage("You currently don't have an invitation request.");
            return false;
        }

        Clan clan = request.getClan();
        player.sendMessage("You've declined invitation request from '" + clan.getString("tag") + "'");
        plugin.getInviteManager().clearRequest(player);

        return true;
    }
}
