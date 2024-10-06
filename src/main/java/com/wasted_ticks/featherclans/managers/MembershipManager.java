package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MembershipManager {

    private final FeatherClans plugin;
    private final DatabaseManager database;
    private final HashMap<UUID, String> players = new HashMap<>();

    public MembershipManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
        this.loadPlayers();
    }

    private void loadPlayers() {
        String query = "SELECT mojang_uuid, c.tag FROM clan_members AS cm LEFT JOIN clans AS c ON c.id = cm.clan_id;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                String tag = results.getString("tag");
                String uuid = results.getString("mojang_uuid");
                if (tag != null && uuid != null) {
                    players.put(UUID.fromString(uuid), tag.toLowerCase());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load players: " + e.getMessage());
        }
    }

    public String getClanByOfflinePlayer(OfflinePlayer player) {
        return players.get(player.getUniqueId());
    }

    public List<OfflinePlayer> getOfflinePlayersByClan(String clan) {
        return players.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(clan))
                .map(entry -> plugin.getServer().getOfflinePlayer(entry.getKey()))
                .collect(Collectors.toList());
    }

    public boolean isOfflinePlayerInClan(OfflinePlayer player) {
        return players.containsKey(player.getUniqueId());
    }

    public boolean isOfflinePlayerInSpecificClan(OfflinePlayer player, String clan) {
        String playerClan = getClanByOfflinePlayer(player);
        return playerClan != null && playerClan.equalsIgnoreCase(clan);
    }

    public boolean addOfflinePlayerToClan(OfflinePlayer player, String tag) {
        String query = "INSERT INTO clan_members (mojang_uuid, clan_id) VALUES (?, (SELECT id FROM clans WHERE lower(tag) = ?));";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, tag.toLowerCase());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                players.put(player.getUniqueId(), tag.toLowerCase());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add player to clan: " + e.getMessage());
        }
        return false;
    }

    public boolean removeOfflinePlayerFromClan(OfflinePlayer player) {
        String query = "DELETE FROM clan_members WHERE mojang_uuid = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getUniqueId().toString());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                players.remove(player.getUniqueId());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove player from clan: " + e.getMessage());
        }
        return false;
    }

    public boolean isOfflinePlayerOfficer(OfflinePlayer player) {
        String query = "SELECT is_officer FROM clan_members WHERE mojang_uuid = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getBoolean("is_officer");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check officer status for player: " + player.getName());
        }
        return false;
    }

    public List<OfflinePlayer> getAllClanMembers() {
        return players.keySet().stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toList());
    }

    public boolean promoteOfficer(OfflinePlayer player) {
        String query = "UPDATE clan_members SET is_officer = true WHERE mojang_uuid = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getUniqueId().toString());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to promote player to officer: " + player.getName());
        }
        return false;
    }

    public boolean demoteOfficer(OfflinePlayer player) {
        String query = "UPDATE clan_members SET is_officer = false WHERE mojang_uuid = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getUniqueId().toString());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to demote player from officer: " + player.getName());
        }
        return false;
    }
}
