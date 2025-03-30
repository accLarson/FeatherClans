package com.wasted_ticks.featherclans.utilities;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

public class AltUtility {
    private final FeatherClans plugin;

    public AltUtility(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public boolean isAlt(OfflinePlayer player) {
        // If LuckPerms is not available, we can't check
        if (plugin.getLuckPermsApi() == null) {
            plugin.getLogger().warning("LuckPerms API not available for alt account checking");
            return false;
        }

        try {
            var user = plugin.getLuckPermsApi().getUserManager().loadUser(player.getUniqueId()).join();

            return user.getCachedData().getPermissionData().checkPermission("group.alt").asBoolean();

        } catch (Exception e) {
            plugin.getLogger().warning("Error checking if player " + player.getName() + " is an alt: " + e.getMessage());
        }

        return false;
    }
}
