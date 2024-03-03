package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
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
        if (plugin.getClanManager().isOfflinePlayerInClan(event.getPlayer())) {
            plugin.getClanManager().updateLastSeenDate(event.getPlayer());
        }
    }
}
