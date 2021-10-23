package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public HelpCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            player.sendMessage(messages.get("clan_help_home"));         // done
            player.sendMessage(messages.get("clan_help_sethome"));      // missing econ
            player.sendMessage(messages.get("clan_help_create"));       // missing econ
            player.sendMessage(messages.get("clan_help_invite"));       // missing econ
            player.sendMessage(messages.get("clan_help_kick"));         //
            player.sendMessage(messages.get("clan_help_disband"));      // done
            player.sendMessage(messages.get("clan_help_confer"));       //
            player.sendMessage(messages.get("clan_help_chat"));         //
            player.sendMessage(messages.get("clan_help_accept"));       // done
            player.sendMessage(messages.get("clan_help_decline"));      // done
            player.sendMessage(messages.get("clan_help_help"));         // done
            player.sendMessage(messages.get("clan_help_list"));         // done
            player.sendMessage(messages.get("clan_help_leaderboard"));         //
            player.sendMessage(messages.get("clan_help_roster"));       //

        }
        return true;
    }
}
