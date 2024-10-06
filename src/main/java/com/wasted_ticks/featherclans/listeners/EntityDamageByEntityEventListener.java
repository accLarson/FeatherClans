package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityEventListener implements Listener {

    private final FeatherClans plugin;
    private final ClanManager manager;


    public EntityDamageByEntityEventListener(FeatherClans plugin) {
        this.plugin = plugin;
        manager = this.plugin.getClanManager();
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {

            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            if(plugin.getMembershipManager().isOfflinePlayerInClan(player) && plugin.getMembershipManager().isOfflinePlayerInClan(damager)){
                if(plugin.getMembershipManager().getClanByOfflinePlayer(player).equals(plugin.getMembershipManager().getClanByOfflinePlayer(damager))) {
                    if (!plugin.getFriendlyFireManager().isAllowingFriendlyFire(player) || !plugin.getFriendlyFireManager().isAllowingFriendlyFire(damager)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
