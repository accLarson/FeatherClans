package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Request;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {

    private final FeatherClansMessages messages;
    private final FeatherClans plugin;
    private final HashMap<String, Request> requests = new HashMap<>();
    private final FeatherClansConfig config;

    public RequestManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.config = plugin.getFeatherClansConfig();
        this.messages = plugin.getFeatherClansMessages();
    }

    public Request getRequest(Player player) {
        return this.requests.get(player.getName());
    }

    public void clearRequest(Player player) {
        this.requests.remove(player.getName());
    }

    public void addRequest(Request.RequestType type, Player target, Player originator, String tag) {
        if (requests.containsKey(target.getName())) return;

        requests.put(target.getName(), new Request(type, target, originator, tag));

        String actionText = "";
        if (type == Request.RequestType.ALLIANCE) actionText = "ally with";
        else if (type == Request.RequestType.MEMBERSHIP) actionText = "join";
        target.sendMessage(messages.get("clan_request_text", Map.of("player", originator.getName(), "action", actionText, "clan", tag)));
        target.sendMessage(messages.get("clan_request_text_response", null));

        if (config.isEconomyEnabled()) {
            double amount = 0.0;
            if (type == Request.RequestType.ALLIANCE) amount = config.getEconomyAlliancePrice();
            else if (type == Request.RequestType.MEMBERSHIP) amount = config.getEconomyMembershipPrice();

            if (type == Request.RequestType.ALLIANCE) {
                target.sendMessage(messages.get("clan_request_text_economy_alliance_both", Map.of("amount", String.valueOf((int) amount))));
            } else if (type == Request.RequestType.MEMBERSHIP) {
                target.sendMessage(messages.get("clan_request_text_economy", Map.of("amount", String.valueOf((int) amount))));
            }
        }

        originator.sendMessage(messages.get("clan_request_text_sent", Map.of("player", target.getName())));

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (requests.containsKey(target.getName())) {
                requests.remove(target.getName());
                target.sendMessage(messages.get("clan_request_expired", Map.of("player", originator.getName())));
            }
        }, config.getClanRequestTimeout() * 20L);
    }
}