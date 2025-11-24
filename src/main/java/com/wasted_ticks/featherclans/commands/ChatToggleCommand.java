package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ChatToggleManager;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatToggleCommand implements CommandExecutor {
    
    private final FeatherClans plugin;
    private final ClanManager clanManager;
    private final ChatToggleManager chatToggleManager;
    private final FeatherClansMessages messages;
    
    public ChatToggleCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.chatToggleManager = plugin.getChatToggleManager();
        this.messages = plugin.getFeatherClansMessages();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("feather.clans.chat")) {
            player.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }
        
        if (!clanManager.isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_chattoggle_no_clan", null));
            return true;
        }
        
        boolean enabled = chatToggleManager.toggleClanChat(player.getUniqueId());
        player.sendMessage(messages.get(enabled ? "clan_chattoggle_enabled" : "clan_chattoggle_disabled", null));
        return true;
    }
}
