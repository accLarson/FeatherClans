package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class ActiveManager {
    private final Set<String> activeClans = new HashSet<>();
    private final FeatherClans plugin;

    public ActiveManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.init();
    }

    private void init() {
        plugin.getClanManager().getClans().forEach(clan -> {
            if (this.assessActiveStatus(clan)) addActiveClan(clan);
        });
    }

    public void addActiveClan(String clanTag) {
        activeClans.add(clanTag.toLowerCase());
        // Probably initiate clan display update
    }

    public void removeActiveClan(String clanTag) {
        activeClans.remove(clanTag.toLowerCase());
        // Probably initiate clan display update
    }

    public boolean isActive(String clanTag) {
        return activeClans.contains(clanTag.toLowerCase());
    }

    public Set<String> getActiveClans() {
        return activeClans;
    }

    public int getActiveMemberCount(List<OfflinePlayer> clanMembers) {
        int count = 0;
        for (OfflinePlayer clanMember : clanMembers) {
            int inactiveDaysThreshold = plugin.getFeatherClansConfig().getClanInactiveDaysThreshold();
            long lastLogin = clanMember.getLastSeen();
            long thresholdTime = System.currentTimeMillis() - (inactiveDaysThreshold * 24L * 60L * 60L * 1000L);
            if (lastLogin > thresholdTime) count++;
        }
        return count;
    }

    public boolean assessActiveStatus(String clanTag) {
        int activeMembersRequirement = plugin.getFeatherClansConfig().getClanActiveMembersRequirement();
        int count = this.getActiveMemberCount(plugin.getClanManager().getOfflinePlayersByClan(clanTag));
        return count >= activeMembersRequirement;
    }

}
