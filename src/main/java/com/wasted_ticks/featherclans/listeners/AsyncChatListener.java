package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanChatLockManager;
import com.wasted_ticks.featherclans.managers.ClanManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public class AsyncChatListener implements Listener {

    private final FeatherClans plugin;
    private final ClanManager clanManager;
    private final ClanChatLockManager chatLockManager;
    private final FeatherClansMessages messages;

    public AsyncChatListener(FeatherClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.chatLockManager = plugin.getClanChatLockManager();
        this.messages = plugin.getFeatherClansMessages();
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        boolean inClanChat = chatLockManager.isInClanChatLock(player);
        boolean inPartnerChat = chatLockManager.isInPartnerChatLock(player);

        if (!plugin.getMembershipManager().isOfflinePlayerInClan(player)) {
            return;
        }

        String clan = plugin.getMembershipManager().getClanByOfflinePlayer(player);
        String partnerClan = clanManager.getPartner(clan);
        String[] clans = new String[]{clan, partnerClan};
        String messageType = inClanChat ? "clan_chat_message" : "clan_partnerchat_message";

        if (inClanChat || inPartnerChat) {
            event.setCancelled(true);
            List<OfflinePlayer> players = plugin.getMembershipManager().getOfflinePlayersByClan(clan);
            for (OfflinePlayer clanPlayer : players) {
                if (clanPlayer.isOnline()) {
                    String targetClan = inClanChat ? clan : clans[0];
                    Player onlinePlayer = clanPlayer.getPlayer();
                    onlinePlayer.sendMessage(messages.get(messageType, Map.of(
                            "tag", clan,
                            "player", player.getName(),
                            "message", message
                    )));
                }
            }

            if (inPartnerChat) {
                List<OfflinePlayer> partnerPlayers = plugin.getMembershipManager().getOfflinePlayersByClan(partnerClan);

                for (OfflinePlayer partnerPlayer : partnerPlayers) {
                    if (partnerPlayer.isOnline()) {
                        Player onlinePartnerPlayer = partnerPlayer.getPlayer();
                        onlinePartnerPlayer.sendMessage(messages.get("clan_partnerchat_message", Map.of(
                                "tag", partnerClan,
                                "clan2", clan,
                                "player", player.getName(),
                                "message", message
                        )));
                    }
                }
            }

            // Send spy messages to operators
            for (OfflinePlayer operator : plugin.getServer().getOperators()) {
                if (operator.isOnline()) {
                    String spyMessageType = inClanChat ? "clan_chat_spy_message" : "clan_partnerchat_spy_message";
                    operator.getPlayer().sendMessage(messages.get(spyMessageType, Map.of(
                            "tag", clan,
                            "clan1", clan,
                            "clan2", partnerClan,
                            "player", player.getName(),
                            "message", message
                    )));
                }
            }
        }
    }
}
