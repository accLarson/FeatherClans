package dev.zerek.featherclans.listeners;

import dev.zerek.featherclans.FeatherClans;
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

        // If player is clanless, return
        if (!plugin.getClanManager().isOfflinePlayerInClan(event.getPlayer())) return;

        String clanTag = plugin.getClanManager().getClanByOfflinePlayer(event.getPlayer());

        // If player is already active, return
        if (plugin.getActiveManager().isActive(event.getPlayer())) return;

        this.plugin.getActiveManager().updateActiveStatus(event.getPlayer(), clanTag);
        
        // Check if player has clan chat enabled and send notification
        if (plugin.getChatToggleManager().hasClanChatEnabled(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(
                plugin.getFeatherClansMessages().get("clan_chattoggle_enabled", null)
            );
        }
        
        // Check if player has ally chat enabled and send notification (only if clan has an ally)
        if (plugin.getChatToggleManager().hasAllyChatEnabled(event.getPlayer().getUniqueId())) {
            if (plugin.getClanManager().hasAlly(clanTag)) {
                event.getPlayer().sendMessage(
                    plugin.getFeatherClansMessages().get("clan_allychattoggle_enabled", null)
                );
            }
        }
    }
}
