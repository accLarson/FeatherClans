package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.util.RequestUtil;
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
        boolean inClan = plugin.getClanManager().isOfflinePlayerInClan(player);
        if(inClan) {
            player.sendMessage("You are currently a member of a clan.");
            return false;
        }

        RequestUtil request = this.plugin.getInviteManager().getRequest(player);
        if(request == null) {
            player.sendMessage("You currently don't have an invitation request.");
            return false;
        }

        String tag = request.getClan();

        player.sendMessage("You've declined invitation request from '" + tag + "'");

        Player originator = request.getOriginator();
        originator.sendMessage("Your request to '" + player.getName() + "' has been declined.");

        plugin.getInviteManager().clearRequest(player);

        return true;
    }
}
