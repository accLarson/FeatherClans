package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
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

        // If players clan is already active, return
        if (plugin.getActiveManager().isActive(clanTag)) return;

        // Check if their clan is now active, if yes, add them to activeClanList
        if (plugin.getActiveManager().assessActiveStatus(clanTag)) plugin.getActiveManager().addActiveClan(clanTag);
    }
}
