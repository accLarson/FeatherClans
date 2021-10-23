package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.util.Request;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class InviteManager {

    private FeatherClans plugin;
    private HashMap<String, Request> requests = new HashMap<>();
    private FeatherClansConfig config;

    public InviteManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.config = plugin.getFeatherClansConfig();
    }

    public Request getRequest(Player player) {
        return this.requests.get(player.getName());
    }

    public void clearRequest(Player player) {
        this.requests.remove(player.getName());
    }

    public boolean invite(Player invitee, Clan clan, Player originator) {
        if(requests.containsKey(invitee.getName())) {
            return false;
        }

        requests.put(invitee.getName(), new Request(clan, originator));

        invitee.sendMessage(originator.getName() + " has requested you join their clan '" + clan.getString("tag") + "'");
        invitee.sendMessage("Reply with '/accept' or '/decline'.");
        originator.sendMessage("Invite sent.");

        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                System.out.println("In runnable for invitation");
                Request request = requests.remove(invitee.getName());
                if(request != null) {
                    invitee.sendMessage("Clan invitation request from " + originator.getName() + " has expired.");
                }
            }
        }, config.getInviteTimeout() * 20);

        return true;
    }

}
