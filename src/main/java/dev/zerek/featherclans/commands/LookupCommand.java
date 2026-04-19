package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LookupCommand implements CommandExecutor {
    
    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final ClanManager manager;
    
    public LookupCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.manager = plugin.getClanManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }
        
        // Check permission
        if (!sender.hasPermission("feather.clans.lookup")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }
        
        // Validate arguments
        if (args.length < 2) {
            sender.sendMessage(messages.get("clan_lookup_error_no_player", null));
            return true;
        }
        
        String playerName = args[1];
        
        // Try to resolve the player
        @SuppressWarnings("deprecation")
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
        
        // Check if player has ever joined the server
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            sender.sendMessage(messages.get("clan_lookup_error_unresolved_player", null));
            return true;
        }
        
        // Check if player is in a clan
        if (!manager.isOfflinePlayerInClan(targetPlayer)) {
            sender.sendMessage(messages.get("clan_lookup_error_not_in_clan", null));
            return true;
        }
        
        // Get the player's clan tag
        String clanTag = manager.getClanByOfflinePlayer(targetPlayer);
        
        // Delegate to RosterCommand with the clan tag
        String[] rosterArgs = new String[] {"roster", clanTag};
        RosterCommand rosterCommand = new RosterCommand(plugin);
        rosterCommand.onCommand(sender, command, label, rosterArgs);
        return true;
    }
}
