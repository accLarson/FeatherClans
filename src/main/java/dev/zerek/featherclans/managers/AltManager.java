package dev.zerek.featherclans.managers;

import dev.zerek.featherclans.FeatherClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AltManager {
    private final FeatherClans plugin;
    private final Map<UUID, Boolean> altStatusCache;

    public AltManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.altStatusCache = new ConcurrentHashMap<>();
        this.initializeCache();
    }

    /**
     * Initialize the cache with all known clan members on plugin startup
     */
    private void initializeCache() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLogger().info("Initializing alt status cache...");
            int count = 0;
            
            for (String clan : plugin.getClanManager().getClans()) {
                for (OfflinePlayer member : plugin.getClanManager().getOfflinePlayersByClan(clan)) {
                    loadAltStatusAsync(member.getUniqueId());
                    count++;
                }
            }
            
            plugin.getLogger().info("Alt status cache initialized with " + count + " players.");
        });
    }

    /**
     * Checks if a player is an alt account (non-blocking, returns cached value)
     * 
     * @param player The player to check
     * @return true if the player is an alt, false otherwise (defaults to false if not cached)
     */
    public boolean isAlt(OfflinePlayer player) {
        return altStatusCache.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Loads alt status for a player asynchronously and updates the cache
     * 
     * @param uuid The UUID of the player to check
     */
    public void loadAltStatusAsync(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isAlt = checkAltStatusBlocking(uuid);
            altStatusCache.put(uuid, isAlt);
        });
    }

    /**
     * Loads alt status for a player asynchronously and updates the cache
     * 
     * @param player The player to check
     */
    public void loadAltStatusAsync(OfflinePlayer player) {
        loadAltStatusAsync(player.getUniqueId());
    }

    /**
     * Blocking method to check alt status via LuckPerms API
     * Should only be called from async tasks
     * 
     * @param uuid The UUID of the player to check
     * @return true if the player is an alt, false otherwise
     */
    private boolean checkAltStatusBlocking(UUID uuid) {
        // If LuckPerms is not available, we can't check
        if (plugin.getLuckPermsApi() == null) {
            return false;
        }

        try {
            var user = plugin.getLuckPermsApi().getUserManager().loadUser(uuid).join();
            return user.getCachedData().getPermissionData().checkPermission("group.alt").asBoolean();

        } catch (Exception e) {
            plugin.getLogger().warning("Error checking if player " + uuid + " is an alt: " + e.getMessage());
        }

        return false;
    }

    /**
     * Refreshes the entire cache (useful for admin reload commands)
     */
    public void refreshCache() {
        altStatusCache.clear();
        initializeCache();
    }

    /**
     * Removes a player from the cache (useful when a player leaves a clan)
     * 
     * @param uuid The UUID of the player to remove
     */
    public void removeFromCache(UUID uuid) {
        altStatusCache.remove(uuid);
    }
}
