package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this.activeClans.remove(clanTag);
        this.getMembersInClan(clanTag).map(Map.Entry::getKey).collect(Collectors.toList()).forEach(this.activeMembers::remove);
    }

    public boolean isActive(String clanTag) {
        return activeClans.containsKey(clanTag.toLowerCase());
    }

    public boolean isActive(OfflinePlayer offlinePlayer) {
        return activeMembers.containsKey(offlinePlayer);
    }

    public int getActiveMemberCount(String clanTag) {
        // Count all active members belonging to this clan regardless of whether the clan is "active"
        return (int) this.getMembersInClan(clanTag).count();
    }
    
    public List<String> getActiveClansOrdered() {
        return activeClans.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void updateActiveStatus(OfflinePlayer offlinePlayer, String clanTag) {
        this.assessActiveMemberStatus(offlinePlayer, clanTag);
        this.assessActiveClanStatus(clanTag);
    }

    /**
     * Helper method to get a stream of active members in a specific clan
     * @param clanTag The clan tag to filter by
     * @return Stream of Map.Entry objects containing active members in the specified clan
     */
    private Stream<Map.Entry<OfflinePlayer, String>> getMembersInClan(String clanTag) {
        return activeMembers.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(clanTag));
    }

    private void assessActiveMemberStatus(OfflinePlayer clanMember, String clanTag) {
        long lastLogin = clanMember.getLastSeen();
        long thresholdTime = System.currentTimeMillis() - (inactiveDaysThreshold * 24L * 60L * 60L * 1000L);
        boolean inClan = plugin.getClanManager().isOfflinePlayerInClan(clanMember);
        if (lastLogin > thresholdTime && inClan) activeMembers.put(clanMember, clanTag);
        else activeMembers.remove(clanMember);

    }

    private void assessActiveClanStatus(String clanTag) {
        int count = Collections.frequency(activeMembers.values(), clanTag);
        if (count >= activeMembersRequirement) activeClans.put(clanTag.toLowerCase(), count);
        else activeClans.remove(clanTag.toLowerCase());
    }

}
