package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;

public class EntityPlaceListener implements Listener {

    private final FeatherClans plugin;

    public EntityPlaceListener(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {

        if (!plugin.getDisplayManager().isPlayerSettingDisplay(event.getPlayer())) return;

        Player player = event.getPlayer();

        if (player == null) return;

        Entity entity = event.getEntity();

        if (plugin.getDisplayManager().isSettingArmorStand(player)) {
            if (!(entity instanceof ArmorStand)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getFeatherClansMessages().get("clan_displaysetup_not_armorstand",null));
            }
            plugin.getDisplayManager().setArmorStand((ArmorStand) event.getEntity());
            plugin.getDisplayManager().nextStep(player);

        }
    }
}
