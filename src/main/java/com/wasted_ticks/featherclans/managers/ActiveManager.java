package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class ActiveManager {
    private final Map<String, Integer> activeClans = new HashMap<>();
    private final Map<OfflinePlayer, String> activeMembers = new HashMap<>();

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

        plugin.getClanManager().getClans().forEach(clan -> {
            plugin.getClanManager().getOfflinePlayersByClan(clan).forEach(clanMember -> {
                this.assessActiveMemberStatus(clanMember, clan);
            });
            this.assessActiveClanStatus(clan);
        });

    }

    public void removeClan(String clanTag) {
        // Remove the clan from activeClans map
        this.activeClans.remove(clanTag);

        // Create a list of players to remove
        List<OfflinePlayer> playersToRemove = this.activeMembers.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(clanTag))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        playersToRemove.forEach(this.activeMembers::remove);
    }

    public boolean isActive(String clanTag) {
        return activeClans.containsKey(clanTag.toLowerCase());
    }

    public boolean isActive(OfflinePlayer offlinePlayer) {
        return activeMembers.containsKey(offlinePlayer);
    }

    public int getActiveCount(String clanTag) {
        return activeClans.getOrDefault(clanTag.toLowerCase(), 0);
    }

    public void updateActiveStatus(OfflinePlayer offlinePlayer, String clanTag) {
        this.assessActiveMemberStatus(offlinePlayer, clanTag);
        this.assessActiveClanStatus(clanTag);
    }

    private void assessActiveMemberStatus(OfflinePlayer clanMember, String clanTag) {
        long lastLogin = clanMember.getLastSeen();
        long thresholdTime = System.currentTimeMillis() - (inactiveDaysThreshold * 24L * 60L * 60L * 1000L);
        if (lastLogin > thresholdTime && plugin.getClanManager().isOfflinePlayerInClan(clanMember)) activeMembers.put(clanMember, clanTag);
        else activeMembers.remove(clanMember);

    }

    private void assessActiveClanStatus(String clanTag) {
        int count = Collections.frequency(activeMembers.values(), clanTag);
        if (count >= activeMembersRequirement) activeClans.put(clanTag.toLowerCase(), count);
        else activeClans.remove(clanTag.toLowerCase());
    }

}
