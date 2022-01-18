package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityEventListener implements Listener {

    private final FeatherClans plugin;

    public EntityDamageByEntityEventListener(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {

            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            ClanManager manager = this.plugin.getClanManager();

            if(manager.isOfflinePlayerInClan(player) && manager.isOfflinePlayerInClan(damager)){
                if(manager.getClanByOfflinePlayer(player).equals(manager.getClanByOfflinePlayer(damager))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
