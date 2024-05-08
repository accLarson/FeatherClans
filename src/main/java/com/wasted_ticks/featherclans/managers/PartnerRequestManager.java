package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.RequestUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PartnerRequestManager {

    private final FeatherClansMessages messages;
    private final FeatherClans plugin;
    private final HashMap<String, RequestUtil> requests = new HashMap<>();
    private final FeatherClansConfig config;

    public PartnerRequestManager(FeatherClans plugin) {
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

    public boolean requestPartnership(Player invitee, String tag, Player originator) {

        if (requests.containsKey(invitee.getName())) {
            originator.sendMessage(messages.get("clan_partner_request_error_already_have_request", null));
            return false;
        }

        for (RequestUtil ru : requests.values()) {
            if (ru.getOriginator().equals(originator)) {
                originator.sendMessage(messages.get("clan_partner_request_error_already_sent_request", null));
                return false;
            }
        }


        requests.put(invitee.getName(), new RequestUtil(tag, originator, RequestUtil.RequestType.PARTNERSHIP_INVITE));

        invitee.sendMessage(messages.get("clan_partner_request_text", Map.of(
                "player", originator.getName(),
                "clan", tag
        )));
        invitee.sendMessage(messages.get("clan_partner_request_text_response", null));


        if (config.isEconomyEnabled()) {
            double amount = config.getEconomyPartnershipPrice();
            invitee.sendMessage(messages.get("clan_partner_request_text_economy", Map.of(
                    "amount", String.valueOf((int) amount)
            )));
        }

        originator.sendMessage(messages.get("clan_partner_request_text_sent", Map.of(
                "player", invitee.getName()
        )));

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            RequestUtil request = requests.remove(invitee.getName());
            if (request != null) {
                invitee.sendMessage(messages.get("clan_partner_request_expired", Map.of(
                        "player", originator.getName()
                )));
            }
        }, config.getClanInviteTimeout() * 20L);
        return true;
    }

}
