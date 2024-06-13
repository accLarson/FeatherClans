package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatModeCommand implements CommandExecutor {
    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ChatModeCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.chatmode")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getClanManager().isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_chatmode_error_not_in_clan", null));
            return true;
        }

        if (plugin.getClanChatModeManager().isInClanChatMode(player)) {
            plugin.getClanChatModeManager().removePlayer(player);
            player.sendMessage(messages.get("clan_chatmode_disabled", null));
        } else {
            plugin.getClanChatModeManager().addPlayer(player);
            player.sendMessage(messages.get("clan_chatmode_enabled", null));
        }

        return true;
    }

}
