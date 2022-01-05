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

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        sender.sendMessage(messages.get("clan_pre_line", null));
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_home", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_sethome", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_create", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_invite", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_disband", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_confer", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_chat", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_accept", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_decline", null));
        }

        sender.sendMessage(messages.get("clan_help_help", null));

        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_list", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_leaderboard", null));
        }
        if(!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_help_roster", null));
        }
        sender.sendMessage(messages.get("clan_line", null));

        return true;
    }
}
