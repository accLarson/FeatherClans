package com.wasted_ticks.featherclans.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FriendlyFireManager {

    private final Set<UUID> friendlyFireEnabled = new HashSet<>();

    /**
     * Toggles friendly fire mode for a player.
     * 
     * @param uuid player UUID
     * @return true if friendly fire is now enabled, false if disabled
     */
    public boolean toggleFriendlyFire(UUID uuid) {
        if (friendlyFireEnabled.contains(uuid)) {
            friendlyFireEnabled.remove(uuid);
            return false;
        } else {
            friendlyFireEnabled.add(uuid);
            return true;
        }
    }

    /**
     * Checks if a player has friendly fire enabled.
     * 
     * @param uuid player UUID
     * @return true if friendly fire is enabled
     */
    public boolean hasFriendlyFireEnabled(UUID uuid) {
        return friendlyFireEnabled.contains(uuid);
    }

    /**
     * Checks if a player is allowing friendly fire (includes permission check).
     * 
     * @param uuid player UUID
     * @param hasForcePermission whether the player has the force friendly fire permission
     * @return true if friendly fire is allowed
     */
    public boolean isAllowingFriendlyFire(UUID uuid, boolean hasForcePermission) {
        if (hasForcePermission) return true;
        return friendlyFireEnabled.contains(uuid);
    }
}
