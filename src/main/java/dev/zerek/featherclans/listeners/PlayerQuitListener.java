package dev.zerek.featherclans.listeners;

import dev.zerek.featherclans.FeatherClans;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final FeatherClans plugin;

    public PlayerQuitListener(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getClanManager().isOfflinePlayerInClan(event.getPlayer())) return;

        String clanTag = plugin.getClanManager().getClanByOfflinePlayer(event.getPlayer());
        
        // Update the last seen cache with the current time
        plugin.getActiveManager().updateActiveStatus(event.getPlayer(), clanTag);
    }
}
