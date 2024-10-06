package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.PVPScoreManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final FeatherClans plugin;
    private final PVPScoreManager pvpScoreManager;

    public PlayerDeathListener(FeatherClans plugin) {
        this.plugin = plugin;
        this.pvpScoreManager = this.plugin.getPVPScoreManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getPlayer().getKiller() != null) {
            Player killer = event.getPlayer().getKiller();
            Player killed = event.getPlayer();

            if (plugin.getMembershipManager().isOfflinePlayerInClan(killer) && plugin.getMembershipManager().isOfflinePlayerInClan(killed)) {
                if (!plugin.getMembershipManager().getClanByOfflinePlayer(killer).equals(plugin.getMembershipManager().getClanByOfflinePlayer(killed))) {
                    pvpScoreManager.addKill(killer, killed);
                }
            }
        }
    }
}
