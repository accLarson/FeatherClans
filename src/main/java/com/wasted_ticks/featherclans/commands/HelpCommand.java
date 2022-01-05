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
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            player.sendMessage(messages.get("clan_pre_line", null));
            player.sendMessage(messages.get("clan_help_home", null));         // done
            player.sendMessage(messages.get("clan_help_sethome", null));      // done
            player.sendMessage(messages.get("clan_help_create", null));       // done
            player.sendMessage(messages.get("clan_help_invite", null));       // done
            player.sendMessage(messages.get("clan_help_kick", null));         // done
            player.sendMessage(messages.get("clan_help_disband", null));      // done
            player.sendMessage(messages.get("clan_help_confer", null));       // done
            player.sendMessage(messages.get("clan_help_chat", null));         // done
            player.sendMessage(messages.get("clan_help_accept", null));       // done
            player.sendMessage(messages.get("clan_help_decline", null));      // done
            player.sendMessage(messages.get("clan_help_help", null));         // done
            player.sendMessage(messages.get("clan_help_list", null));         // done
            player.sendMessage(messages.get("clan_help_leaderboard", null));  //
            player.sendMessage(messages.get("clan_help_roster", null));       //
            player.sendMessage(messages.get("clan_line", null));
        }
        return true;
    }
}
