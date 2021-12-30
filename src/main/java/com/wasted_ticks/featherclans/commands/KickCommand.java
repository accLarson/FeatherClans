package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KickCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public KickCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player"));
            return false;
        }

        Player originator = (Player) sender;
        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(originator);
        if (!leader) {
            originator.sendMessage(messages.get("clan_error_leader"));
            return false;
        }

        if (args.length != 2) {
            //TODO msg
            originator.sendMessage("Error: No player specified to kick.");
            return false;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            //TODO msg
            originator.sendMessage("Error: Unable to resolve player name.");
            return false;
        }

        String tag = this.plugin.getClanManager().getClanByOfflinePlayer(originator);
        if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(player, tag)) {
            //TODO msg
            originator.sendMessage("Error: Player must be in your clan.");
            return false;
        }

        boolean successful = this.plugin.getClanManager().resignOfflinePlayer(player);
        if (!successful) {
            //TODO msg
            originator.sendMessage("error kicking player.");
            return false;
        }

        //TODO msg
        originator.sendMessage("You've kicked " + player.getName() + " from your clan.");
        player.sendMessage("You've been kicked from " + tag);
        return true;

    }
}