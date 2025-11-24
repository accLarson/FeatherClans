package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.managers.ChatToggleManager;
import dev.zerek.featherclans.managers.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AllyChatToggleCommand implements CommandExecutor {
    
    private final FeatherClans plugin;
    private final ClanManager clanManager;
    private final ChatToggleManager chatToggleManager;
    private final FeatherClansMessages messages;
    
    public AllyChatToggleCommand(FeatherClans plugin) {
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
            player.sendMessage(messages.get("clan_allychattoggle_no_clan", null));
            return true;
        }
        
        String clan = clanManager.getClanByOfflinePlayer(player);
        if (!clanManager.hasAlly(clan)) {
            player.sendMessage(messages.get("clan_allychattoggle_no_ally", null));
            return true;
        }
        
        boolean enabled = chatToggleManager.toggleAllyChat(player.getUniqueId());
        player.sendMessage(messages.get(enabled ? "clan_allychattoggle_enabled" : "clan_allychattoggle_disabled", null));
        return true;
    }
}
