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

public class ConferCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ConferCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player"));
            return false;
        }

        Player originator = (Player) sender;
        if(args.length != 2) {
            originator.sendMessage(messages.get("clan_confer_no_player"));
            return false;
        }

        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(originator);
        if(!leader) {
            originator.sendMessage(messages.get("clan_error_leader"));
            return false;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if(player == null) {
            originator.sendMessage(messages.get("clan_confer_unresolved_player"));
            return false;
        }

        String clan = this.plugin.getClanManager().getClanByOfflinePlayer(originator);
        if(!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(player, clan)) {
            originator.sendMessage(messages.get("clan_confer_not_in_clan"));
            return false;
        }

        boolean successful = this.plugin.getClanManager().setClanLeader(clan, player);
        if(successful) {
            //TODO: minimessage placeholder for <player>
            originator.sendMessage(messages.get("clan_confer_success_originator"));
            //TODO: minimessage placeholder for <player> <clan>
            player.sendMessage(messages.get("clan_confer_success_player"));
            return true;
        } else {
            originator.sendMessage(messages.get("clan_confer_error_generic"));
            return false;
        }




    }
}
