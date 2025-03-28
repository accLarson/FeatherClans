package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActiveManager {
    private final Map<String, Integer> activeClans = new HashMap<>();
    private final Map<UUID, String> activeMembers = new HashMap<>();

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
        this.getActiveMembersInClan(clanTag).forEach(this.activeMembers::remove);
    }

    public boolean isActive(String clanTag) {
        return activeClans.containsKey(clanTag.toLowerCase());
    }

    public boolean isActive(OfflinePlayer offlinePlayer) {
        return activeMembers.containsKey(offlinePlayer);
    }

    public int getActiveMemberCount(String clanTag) {
        // Count all active members belonging to this clan regardless of whether the clan is "active"
        return this.getActiveMembersInClan(clanTag).size();
    }

    public List<UUID> getActiveMembersInClan(String clanTag) {
        return activeMembers.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(clanTag))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> getActiveClansOrdered() {
        return activeClans.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void updateActiveStatus(OfflinePlayer offlinePlayer, String clanTag) {
        Map<String, Integer> activeClansCopy = new HashMap<>(activeClans);
        this.assessActiveMemberStatus(offlinePlayer, clanTag);
        this.assessActiveClanStatus(clanTag);
        if (!activeClansCopy.equals(this.activeClans)) this.plugin.getDisplayManager().resetDisplays();
    }

    private void assessActiveMemberStatus(OfflinePlayer clanMember, String clanTag) {
        long lastLogin = clanMember.getLastSeen();
        long thresholdTime = System.currentTimeMillis() - (inactiveDaysThreshold * 24L * 60L * 60L * 1000L);
        boolean inClan = plugin.getClanManager().isOfflinePlayerInSpecificClan(clanMember, clanTag);
        if (lastLogin > thresholdTime && inClan && !this.isAlt(clanMember)) activeMembers.put(clanMember.getUniqueId(), clanTag);
        else activeMembers.remove(clanMember.getUniqueId());
    }

    private void assessActiveClanStatus(String clanTag) {
        int count = Collections.frequency(activeMembers.values(), clanTag);
        if (count >= activeMembersRequirement) activeClans.put(clanTag.toLowerCase(), count);
        else activeClans.remove(clanTag.toLowerCase());
    }

    public boolean isAlt(OfflinePlayer player) {
        // If LuckPerms is not available, we can't check
        if (plugin.getLuckPermsApi() == null) {
            plugin.getLogger().warning("LuckPerms API not available for alt account checking");
            return false;
        }

        try {
            // Load the user data (this returns a CompletableFuture)
            // We need to join() to wait for the result since this method isn't async
            var user = plugin.getLuckPermsApi().getUserManager().loadUser(player.getUniqueId()).join();
            
            // Check if the user has the "group.alt" permission or is in the "alt" group
            if (user.getCachedData().getPermissionData().checkPermission("group.alt").asBoolean()) return true;
                    
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking if player " + player.getName() + " is an alt: " + e.getMessage());
        }
        
        return false;
    }


}
