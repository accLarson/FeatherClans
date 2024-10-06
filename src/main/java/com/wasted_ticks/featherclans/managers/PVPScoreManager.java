package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PVPScoreManager {

    private final FeatherClans plugin;
    private final ClanManager clanManager;
    private final MembershipManager membershipManager;
    private final DatabaseManager databaseManager;

    Map<OfflinePlayer,Map<OfflinePlayer,Integer>> killMap = new HashMap<>();
    Map<OfflinePlayer,Integer> scoreMap = new HashMap<>();

    public PVPScoreManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.membershipManager = plugin.getMembershipManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.init();
    }

    private void init() {
        membershipManager.getAllClanMembers().forEach(offlinePlayer -> killMap.put(offlinePlayer, getClanMemberKillData(offlinePlayer)));
        killMap.forEach((killer, kills) -> this.updateScore(killer));
    }

    private void updateScore(OfflinePlayer offlinePlayer) {
        scoreMap.put(offlinePlayer,0);
        killMap.get(offlinePlayer).forEach((killed, killCount) -> {
            if (killCount > killMap.get(killed).getOrDefault(offlinePlayer,0)) {
                scoreMap.put(offlinePlayer,scoreMap.getOrDefault(offlinePlayer,0) + 1);
            }
        });
    }

    public void addKill(OfflinePlayer killer, OfflinePlayer killed) {
        killMap.compute(killer, (key, value) -> {
            if (value == null) value = new HashMap<>(Map.of(killed, 1));
            else value.put(killed, value.getOrDefault(killed, 0) + 1);

            this.updateScore(killer);
            this.updateScore(killed);

            return value;
        });
        addKillRecord(killer, killed);
    }

    public int getScore(OfflinePlayer offlinePlayer) {
        return scoreMap.getOrDefault(offlinePlayer,0);
    }

    public int getScore(String tag){
        return membershipManager.getOfflinePlayersByClan(tag).stream().mapToInt(member -> scoreMap.getOrDefault(member, 0)).sum();
    }

    public Map<OfflinePlayer, Integer> getKills(OfflinePlayer offlinePlayer) {
        return killMap.getOrDefault(offlinePlayer, new HashMap<>());
    }

    public Map<OfflinePlayer, Integer> getClanMemberKillData(OfflinePlayer player) {
        Map<OfflinePlayer, Integer> killData = new HashMap<>();
        String query = "SELECT cm.mojang_uuid, COUNT(*) as kill_count FROM clan_kills ck " +
                       "JOIN clan_members cm ON ck.victim_id = cm.id " +
                       "WHERE ck.killer_id = (SELECT id FROM clan_members WHERE mojang_uuid = ?) " +
                       "GROUP BY cm.mojang_uuid";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet results = statement.executeQuery();
            
            while (results.next()) {
                UUID victimUUID = UUID.fromString(results.getString("mojang_uuid"));
                int killCount = results.getInt("kill_count");
                killData.put(Bukkit.getOfflinePlayer(victimUUID), killCount);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve kill data for player: " + player.getName());
        }
        
        return killData;
    }

    private void addKillRecord(OfflinePlayer killer, OfflinePlayer killed) {
        String query = "INSERT INTO clan_kills (killer_id, victim_id) VALUES " +
                       "((SELECT id FROM clan_members WHERE mojang_uuid = ?), " +
                       "(SELECT id FROM clan_members WHERE mojang_uuid = ?))";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, killer.getUniqueId().toString());
            statement.setString(2, killed.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add kill record for killer: " + killer.getName() + ", killed: " + killed.getName());
        }
    }
}

