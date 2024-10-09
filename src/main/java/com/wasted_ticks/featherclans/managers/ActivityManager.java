package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActivityManager {

    private final FeatherClans plugin;
    private final DatabaseManager database;
    private final Map<UUID, Boolean> memberActivityStatus;

    public ActivityManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
        this.memberActivityStatus = new HashMap<>();
        initializeMemberActivityStatus();
    }

    private void initializeMemberActivityStatus() {
        String query = "SELECT mojang_uuid, is_active FROM clan_members;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("mojang_uuid"));
                boolean isActive = result.getBoolean("is_active");
                memberActivityStatus.put(uuid, isActive);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize member activity status: " + e.getMessage());
        }
    }

    public void updatePlayerActivity(OfflinePlayer player) {
        updateLastSeenDate(player);
        boolean isActive = (System.currentTimeMillis() - player.getLastPlayed()) / 86400000 < plugin.getFeatherClansConfig().getInactiveDays();
        setOfflinePlayerActive(player, isActive);
    }

    public boolean isClanActive(String clan) {
        if (clan == null || clan.isEmpty()) {
            plugin.getLogger().warning("Attempted to calculate active status for null or empty clan tag");
            return false;
        }
        int activeCount = (int) plugin.getMembershipManager().getOfflinePlayersByClan(clan).stream()
                .filter(this::isOfflinePlayerActive).count();
        return activeCount >= plugin.getFeatherClansConfig().getClanActiveStatusCount();
    }

    public int getActiveMemberCount(String clan) {
        return (int) plugin.getMembershipManager().getOfflinePlayersByClan(clan).stream()
                .filter(this::isOfflinePlayerActive).count();
    }

    public void updateLastSeenDate(OfflinePlayer offlinePlayer) {
        String sql = "UPDATE clan_members SET last_seen_date = CURRENT_TIMESTAMP WHERE mojang_uuid = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement update = connection.prepareStatement(sql)) {
            update.setString(1, offlinePlayer.getUniqueId().toString());
            update.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update last seen date for " + offlinePlayer.getName());
        }
    }

    public boolean isOfflinePlayerActive(OfflinePlayer offlinePlayer) {
        String query = "SELECT is_active FROM clan_members WHERE mojang_uuid = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, offlinePlayer.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getBoolean("is_active");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check activity status for player: " + offlinePlayer.getName());
        }
        return false;
    }

    private void setOfflinePlayerActive(OfflinePlayer player, boolean isActive) {
        String query = "UPDATE clan_members SET is_active = ? WHERE mojang_uuid = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, isActive);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update player activity status: " + e.getMessage());
        }
    }
}
