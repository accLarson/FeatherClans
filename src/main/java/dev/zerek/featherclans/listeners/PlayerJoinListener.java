package dev.zerek.featherclans.listeners;

import dev.zerek.featherclans.FeatherClans;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final FeatherClans plugin;

    public PlayerJoinListener(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // If player is not in a clan, return early
        if (!plugin.getClanManager().isOfflinePlayerInClan(event.getPlayer())) return;

        String clanTag = plugin.getClanManager().getClanByOfflinePlayer(event.getPlayer());

        // Schedule chat toggle notifications to be sent after 1 second (20 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check if player is still online before sending messages
            if (!event.getPlayer().isOnline()) return;
            
            // Check if player has clan chat enabled and send notification
            if (plugin.getChatToggleManager().hasClanChatEnabled(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(plugin.getFeatherClansMessages().get("clan_chattoggle_enabled", null));
            }
            
            // Check if player has ally chat enabled and send notification
            if (plugin.getChatToggleManager().hasAllyChatEnabled(event.getPlayer().getUniqueId()) 
                    && plugin.getClanManager().hasAlly(clanTag)) {
                event.getPlayer().sendMessage(plugin.getFeatherClansMessages().get("clan_allychattoggle_enabled", null));
            }
        }, 20L); // 20 ticks = 1 second
        
        // Update active status if player is not already marked as active
        if (!plugin.getActiveManager().isActive(event.getPlayer())) {
            this.plugin.getActiveManager().updateActiveStatus(event.getPlayer(), clanTag);
        }
    }
}
