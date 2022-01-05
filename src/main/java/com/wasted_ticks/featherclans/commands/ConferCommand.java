package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ConferCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ConferCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if(!sender.hasPermission("feather.clans.confer")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }


        Player originator = (Player) sender;
        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(originator);
        if (!leader) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_confer_no_player", null));
            return true;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            originator.sendMessage(messages.get("clan_confer_unresolved_player", null));
            return true;
        }

        String clan = this.plugin.getClanManager().getClanByOfflinePlayer(originator);
        if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(player, clan)) {
            originator.sendMessage(messages.get("clan_confer_not_in_clan", null));
            return true;
        }

        boolean successful = this.plugin.getClanManager().setClanLeader(clan, player);
        if (successful) {
            originator.sendMessage(messages.get("clan_confer_success_originator", Map.of(
                    "player", player.getName()
            )));
            player.sendMessage(messages.get("clan_confer_success_player", Map.of(
                    "player", originator.getName(),
                    "clan", clan
            )));
        } else {
            originator.sendMessage(messages.get("clan_confer_error_generic", null));
        }
        return true;

    }
}
