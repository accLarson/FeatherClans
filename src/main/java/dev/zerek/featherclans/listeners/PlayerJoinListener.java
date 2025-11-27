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
        if (!plugin.getClanManager().isOfflinePlayerInClan(event.getPlayer())) return;

        String clanTag = plugin.getClanManager().getClanByOfflinePlayer(event.getPlayer());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!event.getPlayer().isOnline()) return;
            
            if (plugin.getChatToggleManager().hasClanChatEnabled(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(plugin.getFeatherClansMessages().get("clan_chattoggle_enabled", null));
            }
            
            if (plugin.getChatToggleManager().hasAllyChatEnabled(event.getPlayer().getUniqueId())
                    && plugin.getClanManager().hasAlly(clanTag)) {
                event.getPlayer().sendMessage(plugin.getFeatherClansMessages().get("clan_allychattoggle_enabled", null));
            }
        }, 20L);
        
        if (!plugin.getActiveManager().isActive(event.getPlayer())) {
            this.plugin.getActiveManager().updateActiveStatus(event.getPlayer(), clanTag);
        }
    }
}
