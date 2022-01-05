package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.util.RequestUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class InviteManager {

    private final FeatherClansMessages messages;
    private FeatherClans plugin;
    private HashMap<String, RequestUtil> requests = new HashMap<>();
    private FeatherClansConfig config;

    public InviteManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.config = plugin.getFeatherClansConfig();
        this.messages = plugin.getFeatherClansMessages();
    }

    public RequestUtil getRequest(Player player) {
        return this.requests.get(player.getName());
    }

    public void clearRequest(Player player) {
        this.requests.remove(player.getName());
    }

    public boolean invite(Player invitee, String tag, Player originator) {

        if (requests.containsKey(invitee.getName())) {
            return false;
        }

        requests.put(invitee.getName(), new RequestUtil(tag, originator));

        invitee.sendMessage(messages.get("clan_invite_text", Map.of(
                "player", originator.getName(),
                "clan", tag
        )));
        invitee.sendMessage(messages.get("clan_invite_text_response", null));
        originator.sendMessage(messages.get("clan_invite_text_sent", Map.of(
                "player", invitee.getName()
        )));

        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                RequestUtil request = requests.remove(invitee.getName());
                if (request != null) {
                    invitee.sendMessage(messages.get("clan_invite_expired", Map.of(
                            "player", originator.getName()
                    )));
                }
            }
        }, config.getClanInviteTimeout() * 20);

        return true;
    }

}
