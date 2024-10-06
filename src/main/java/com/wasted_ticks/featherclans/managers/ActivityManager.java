package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActivityManager {

    private final FeatherClans plugin;
    private final DatabaseManager database;

    public ActivityManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
    }

    public void updatePlayerActivity(OfflinePlayer player) {
        updateLastSeenDate(player);
        boolean isActive = (System.currentTimeMillis() - player.getLastPlayed()) / 86400000 < plugin.getFeatherClansConfig().getInactiveDays();
        setOfflinePlayerActive(player, isActive);
        updateClanActiveStatus(plugin.getMembershipManager().getClanByOfflinePlayer(player));
    }

    public void updateClanActiveStatus(String clan) {
        int activeCount = (int) plugin.getMembershipManager().getOfflinePlayersByClan(clan).stream()
                .filter(this::isOfflinePlayerActive).count();
        boolean isActive = activeCount >= plugin.getFeatherClansConfig().getClanActiveStatusCount();
        String query = "UPDATE clans SET is_active = ? WHERE tag = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, isActive);
            statement.setString(2, clan);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update active status for clan: " + clan);
        }
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

    public boolean isClanActiveStatus(String clan) {
        String query = "SELECT is_active FROM clans WHERE tag = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, clan);
            ResultSet result = statement.executeQuery();
            return result.next() && result.getBoolean("is_active");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check active status for clan: " + clan);
        }
        return false;
    }
}
