package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatLockCommand implements CommandExecutor {
    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ChatLockCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.chatlock")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getClanManager().isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_chatlock_error_not_in_clan", null));
            return true;
        }

        if (plugin.getClanChatLockManager().isInClanChatLock(player)) {
            plugin.getClanChatLockManager().removePlayer(player);
            player.sendMessage(messages.get("clan_chatlock_disabled", null));
        } else {
            plugin.getClanChatLockManager().addPlayer(player);
            player.sendMessage(messages.get("clan_chatlock_enabled", null));
        }

        return true;
    }

}
