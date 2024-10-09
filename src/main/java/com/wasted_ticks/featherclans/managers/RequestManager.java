package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.RequestUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {

    private final FeatherClansMessages messages;
    private final FeatherClans plugin;
    private final HashMap<String, RequestUtil> requests = new HashMap<>();
    private final FeatherClansConfig config;

    public RequestManager(FeatherClans plugin) {
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

    public boolean createRequest(Player invitee, String tag, Player originator, RequestUtil.RequestType type) {
        if (requests.containsKey(invitee.getName())) {
            return false;
        }

        requests.put(invitee.getName(), new RequestUtil(tag, originator, type));

        String messageKey = type == RequestUtil.RequestType.CLAN_INVITE ? "clan_invite_text" : "clan_partner_request_text";
        invitee.sendMessage(messages.get(messageKey, Map.of(
                "player", originator.getName(),
                "clan", tag
        )));
        invitee.sendMessage(messages.get(type == RequestUtil.RequestType.CLAN_INVITE ? "clan_invite_text_response" : "clan_partner_request_text_response", null));

        if (config.isEconomyEnabled()) {
            double amount = type == RequestUtil.RequestType.CLAN_INVITE ? config.getEconomyInvitePrice() : config.getEconomyPartnershipPrice();
            invitee.sendMessage(messages.get(type == RequestUtil.RequestType.CLAN_INVITE ? "clan_invite_text_economy" : "clan_partner_request_text_economy", Map.of(
                    "amount", String.valueOf((int) amount)
            )));
        }

        originator.sendMessage(messages.get(type == RequestUtil.RequestType.CLAN_INVITE ? "clan_invite_text_sent" : "clan_partner_request_text_sent", Map.of(
                "player", invitee.getName()
        )));

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            RequestUtil request = requests.remove(invitee.getName());
            if (request != null) {
                invitee.sendMessage(messages.get(type == RequestUtil.RequestType.CLAN_INVITE ? "clan_invite_expired" : "clan_partner_request_expired", Map.of(
                        "player", originator.getName()
                )));
                originator.sendMessage(messages.get(type == RequestUtil.RequestType.CLAN_INVITE ? "clan_invite_expired_sender" : "clan_partner_request_expired_sender", Map.of(
                        "player", invitee.getName()
                )));
            }
        }, config.getClanInviteTimeout() * 20L);
        return true;
    }

    public void declineRequest(Player player) {
        RequestUtil request = getRequest(player);
        if (request != null) {
            Player originator = request.getOriginator();
            String messageKey = request.getType() == RequestUtil.RequestType.CLAN_INVITE ? "clan_decline_originator" : "clan_partner_request_decline_originator";
            originator.sendMessage(messages.get(messageKey, Map.of(
                    "player", player.getName(),
                    "clan", request.getClan()
            )));

            messageKey = request.getType() == RequestUtil.RequestType.CLAN_INVITE ? "clan_decline_success" : "clan_partner_request_decline_success";
            player.sendMessage(messages.get(messageKey, Map.of(
                    "clan", request.getClan()
            )));

            clearRequest(player);
        } else {
            player.sendMessage(messages.get("clan_decline_no_invitation", null));
        }
    }
}
