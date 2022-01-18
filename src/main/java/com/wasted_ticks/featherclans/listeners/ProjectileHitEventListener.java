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

    public ProjectileHitEventListener(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if(projectile.getShooter() != null
            && projectile.getShooter() instanceof Player
            && event.getHitEntity() != null
            && event.getHitEntity() instanceof Player) {

            Player player = (Player) event.getHitEntity();
            Player damager = (Player) projectile.getShooter();

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
