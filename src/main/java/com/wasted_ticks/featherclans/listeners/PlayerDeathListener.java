package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final FeatherClans plugin;
    private final ClanManager manager;


    public PlayerDeathListener(FeatherClans plugin) {
        this.plugin = plugin;
        this.manager = this.plugin.getClanManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getPlayer().getKiller() != null) {
            Player killer = event.getPlayer().getKiller();
            Player killed = event.getPlayer();

            if (manager.isOfflinePlayerInClan(killer) && manager.isOfflinePlayerInClan(killed)) {
                if (!manager.getClanByOfflinePlayer(killer).equals(manager.getClanByOfflinePlayer(killed))) {
                    plugin.getPVPScoreManager().addKill(killer,killed);
                    manager.addKillRecord(killer,killed);
                }
            }
        }
    }
}
