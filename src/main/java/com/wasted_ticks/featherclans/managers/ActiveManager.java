package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class ActiveManager {
    private final Map<String, Integer> activeClans = new HashMap<>();
    private final FeatherClans plugin;
    private int activeMembersRequirement;
    private int inactiveDaysThreshold;


    public ActiveManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.init();

    }

    private void init() {
        this.activeMembersRequirement = this.plugin.getFeatherClansConfig().getClanActiveMembersRequirement();
        this.inactiveDaysThreshold = this.plugin.getFeatherClansConfig().getClanInactiveDaysThreshold();

        plugin.getClanManager().getClans().forEach(this::assessActiveStatus);
    }

    public void removeClan(String clanName) {
        this.activeClans.remove(clanName.toLowerCase());
    }

    public boolean isActive(String clanTag) {
        return activeClans.containsKey(clanTag.toLowerCase());
    }

    public Set<String> getActiveClans() {
        return activeClans.keySet();
    }

    public int getActiveMemberCount(List<OfflinePlayer> clanMembers) {
        int count = 0;
        for (OfflinePlayer clanMember : clanMembers) {
            long lastLogin = clanMember.getLastSeen();
            long thresholdTime = System.currentTimeMillis() - (inactiveDaysThreshold * 24L * 60L * 60L * 1000L);
            if (lastLogin > thresholdTime) count++;
        }
        return count;
    }

    public void assessActiveStatus(String clanTag) {
        int count = this.getActiveMemberCount(plugin.getClanManager().getOfflinePlayersByClan(clanTag));

        if (count >= activeMembersRequirement) {
            activeClans.put(clanTag.toLowerCase(), count);
        }
        else {
            activeClans.remove(clanTag.toLowerCase());
        }
    }

}
