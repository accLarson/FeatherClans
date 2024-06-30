package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final FeatherClans plugin;

    public BlockPlaceListener(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){

        if (!plugin.getDisplayManager().isPlayerSettingDisplay(event.getPlayer())) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (plugin.getDisplayManager().isSettingBanner(player)) {
            if (!(block instanceof Banner)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getFeatherClansMessages().get("clan_displaysetup_not_banner",null));
            }
            plugin.getDisplayManager().setBanner((Banner) event.getBlock());
            plugin.getDisplayManager().nextStep(player);

        }

        else if (plugin.getDisplayManager().isSettingSign(player)) {
            if (!(block instanceof Sign)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getFeatherClansMessages().get("clan_displaysetup_not_sign",null));
            }
            plugin.getDisplayManager().setSign((Sign) event.getBlock());
            plugin.getDisplayManager().nextStep(player);


        }
    }
}
