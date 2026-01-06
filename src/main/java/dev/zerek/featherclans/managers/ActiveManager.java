package dev.zerek.featherclans.managers;

import dev.zerek.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class ActiveManager {
    private final Map<String, Integer> activeClans = new HashMap<>();
    private final Map<UUID, String> activeMembers = new HashMap<>();
    private final Map<UUID, Long> lastSeenCache = new HashMap<>();

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
                lastSeenCache.put(clanMember.getUniqueId(), clanMember.getLastSeen());
                this.assessActiveMemberStatus(clanMember, clan);
            });
            this.assessActiveClanStatus(clan);
        });

    }

    public void removeClan(String clanTag) {
        this.activeClans.remove(clanTag);
        this.getActiveMembersInClan(clanTag).forEach(this.activeMembers::remove);
        this.getActiveMembersInClan(clanTag).forEach(this.lastSeenCache::remove);
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
        // First, remove any existing entry for this player to prevent stale data
        activeMembers.remove(offlinePlayer.getUniqueId());
        lastSeenCache.put(offlinePlayer.getUniqueId(), System.currentTimeMillis());

        Map<String, Integer> activeClansCopy = new HashMap<>(activeClans);
        this.assessActiveMemberStatus(offlinePlayer, clanTag);
        this.assessActiveClanStatus(clanTag);
        if (!activeClansCopy.equals(this.activeClans)) this.plugin.getDisplayManager().resetDisplays();
    }
    
    public void removePlayerFromActive(UUID playerUUID, String clanTag) {
        if (activeMembers.remove(playerUUID) != null) {
            // Player was in the active members map, reassess the clan status
            lastSeenCache.remove(playerUUID);
            this.assessActiveClanStatus(clanTag);
            plugin.getLogger().fine("Removed player " + playerUUID + " from active members of clan: " + clanTag);
        }
    }

    private void assessActiveMemberStatus(OfflinePlayer clanMember, String clanTag) {
        long lastLogin = clanMember.getLastSeen();
        long thresholdTime = System.currentTimeMillis() - (inactiveDaysThreshold * 24L * 60L * 60L * 1000L);
        boolean inClan = plugin.getClanManager().isOfflinePlayerInSpecificClan(clanMember, clanTag);
        
        if (lastLogin > thresholdTime && inClan && !plugin.getAltManager().isAlt(clanMember)) {
            activeMembers.put(clanMember.getUniqueId(), clanTag.toLowerCase());
            plugin.getLogger().fine("Added active member: " + clanMember.getName() + " to clan: " + clanTag);
        } else {
            if (activeMembers.remove(clanMember.getUniqueId()) != null) {
                plugin.getLogger().fine("Removed active member: " + clanMember.getName() + " from clan: " + clanTag);
            }
        }
    }

    private void assessActiveClanStatus(String clanTag) {
        // Use case-insensitive comparison instead of Collections.frequency
        int count = (int) activeMembers.values().stream()
                .filter(tag -> tag.equalsIgnoreCase(clanTag))
                .count();
        
        plugin.getLogger().fine("Assessing clan status for " + clanTag + ": " + count + " active members");
        
        if (count >= activeMembersRequirement) activeClans.put(clanTag.toLowerCase(), count);
        else activeClans.remove(clanTag.toLowerCase());
    }

    /**
     * Gets the cached last seen time for a player
     * 
     * @param uuid The UUID of the player
     * @return The last seen timestamp in milliseconds, or 0 if not cached
     */
    public long getLastSeen(UUID uuid) {
        Long lastSeen = lastSeenCache.get(uuid);
        if (lastSeen != null) return lastSeen;
        // Fallback to Bukkit API if not in cache
        return plugin.getServer().getOfflinePlayer(uuid).getLastSeen();
    }
}
