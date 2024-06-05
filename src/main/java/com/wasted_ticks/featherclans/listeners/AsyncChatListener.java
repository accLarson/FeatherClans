package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AsyncChatListener implements Listener {

    private final FeatherClans plugin;

    public AsyncChatListener(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent e) {
        if (plugin.getClanManager().isOfflinePlayerInClan(e.getPlayer()) && plugin.getClanChatModeManager().isInClanChatMode(e.getPlayer())) {

            e.setCancelled(true);

            String plainText = PlainTextComponentSerializer.plainText().serialize(e.originalMessage());
            String[] args = plainText.split("\\s+");

            plugin.getClanChatModeManager().sendClanChat(e.getPlayer(),args);
        }

    }
}
