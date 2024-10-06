package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitEventListener implements Listener {

    private final FeatherClans plugin;
    private final ClanManager manager;

    public ProjectileHitEventListener(FeatherClans plugin) {
        this.plugin = plugin;
        manager = this.plugin.getClanManager();
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if(projectile.getShooter() != null
            && projectile.getShooter() instanceof Player
            && event.getHitEntity() != null
            && event.getHitEntity() instanceof Player
            && projectile.getShooter() != event.getHitEntity()) {

            Player player = (Player) event.getHitEntity();
            Player damager = (Player) projectile.getShooter();



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
