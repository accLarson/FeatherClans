package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.util.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ClanManager {

    private static final HashMap<UUID, String> players = new HashMap<>();
    private static final HashMap<String, UUID> clans = new HashMap<>();
    private static final List<UUID> officers = new ArrayList<>();
    private static final Set<UUID> activeMembers = new HashSet<>();
    private static final List<String> activeClans = new ArrayList<>();
    private final FeatherClans plugin;
    private final DatabaseManager database;

    public ClanManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
        this.load();
    }

    private void load() {
        loadPlayers();
        loadClans();
        loadOfficers();
        loadActive();
    }

    private void loadPlayers() {
        String string = "SELECT mojang_uuid, c.tag  FROM clan_members AS cm left JOIN clans AS c ON c.id = cm.clan_id;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(string);
            ResultSet results = statement.executeQuery())
        {
            if(results != null) {
                while (results.next()) {
                    String tag = results.getString("tag");
                    String uuid = results.getString("mojang_uuid");
                    if(tag != null && uuid != null) {
                        players.put(UUID.fromString(uuid), tag.toLowerCase());
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load players.");
        } catch(IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to parse UUID into player cache.");
        }
    }



    private void loadClans() {
        String string = "SELECT `tag`, `leader_uuid` FROM clans;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(string);
            ResultSet results = statement.executeQuery())
        {
            if(results != null) {
                while (results.next()) {
                    String tag = results.getString("tag");
                    String uuid = results.getString("leader_uuid");
                    if(tag != null && uuid != null) {
                        clans.put(tag.toLowerCase(), UUID.fromString(uuid));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load clans.");
        } catch(IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to parse UUID into clan cache.");
        }
    }

    private void loadOfficers() {
        String query = "SELECT mojang_uuid FROM clan_members WHERE is_officer = 1;";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet results = statement.executeQuery()) {

            if(results != null) {
                while (results.next()) {
                    String uuid = results.getString("mojang_uuid");
                    if (uuid != null) officers.add(UUID.fromString(uuid));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load officers.");
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to parse UUID for officers.");
        }
    }

    private void loadActive() {
        String query = "SELECT mojang_uuid FROM clan_members WHERE is_active = 1;";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet results = statement.executeQuery()) {

            if(results != null) {
                while (results.next()) {
                    String uuid = results.getString("mojang_uuid");
                    if (uuid != null) activeMembers.add(UUID.fromString(uuid));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load active clan members.");
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to parse UUID for active clan members.");
        }
    }


    /**
     * Gets a list of all clans.
     *
     * @return list of clans
     */
    public List<String> getClans() {
        return List.copyOf(clans.keySet());
    }

    /**
     * Gets a list of all clans members.
     *
     * @return list of clans
     */
    public List<OfflinePlayer> getAllClanMembers() {
        return players.keySet().stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toList());
    }

    /**
     * Gets the clan associated with the offline player.
     *
     * @param player
     * @return clan, will return null if offline player is not a member of a clan.
     */
    public String getClanByOfflinePlayer(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return players.get(uuid);
    }

    /**
     * Gets a list of offline players associated with a clan.
     *
     * @param clan
     * @return a list of offline players.
     */
    public List<OfflinePlayer> getOfflinePlayersByClan(String clan) {
        return players.entrySet().stream()
                .filter(entry -> entry.getValue().equals(clan))
//                .filter(entry -> entry.getValue().equalsIgnoreCase(clan))
                .map(entry -> Bukkit.getOfflinePlayer(entry.getKey()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the clan home location for a given clan.
     *
     * @param tag
     * @return location
     */
    public Location getClanHome(String tag) {
        String query = "SELECT `home` FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement select = connection.prepareStatement(query)) {

            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();
            if(results != null && results.next()) {
                String home = results.getString("home");
                if(home != null) {
                    return SerializationUtil.stringToLocation(home);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed check clan home for: " + tag.toLowerCase());
        }
        return null;
    }

    /**
     * Determines if an offline player is a member of a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean isOfflinePlayerInClan(OfflinePlayer player) {
        return players.containsKey(player.getUniqueId());
    }

    public boolean isOfflinePlayerInSpecificClan(OfflinePlayer player, String clan) {
        String playerClan = players.get(player.getUniqueId());
        return playerClan != null && playerClan.equalsIgnoreCase(clan);
    }

    /**
     * Determines if an offline player is a leader of a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean isOfflinePlayerLeader(OfflinePlayer player) {
        return clans.containsValue(player.getUniqueId());
    }

    /**
     * Determines if a given clan has a home.
     *
     * @param tag
     * @return boolean
     */
    public boolean hasClanHome(String tag) {
        String query = "SELECT `home` FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement select = connection.prepareStatement(query)) {

            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();
            if(results != null && results.next()) {
                String home = results.getString("home");
                if(home != null) {
                    return true;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed check clan home for: " + tag);
        }
        return false;
    }

    /**
     * Creates a new clan.
     *
     * @param player leader
     * @param stack  banner
     * @param tag
     * @return returns the newly created clan or null if unsuccessful.
     */
    public boolean createClan(OfflinePlayer player, ItemStack stack, String tag) {

        ItemStack clone = stack.clone();
        clone.setAmount(1);
        String data = SerializationUtil.stackToString(clone);
        UUID uuid = player.getUniqueId();

        String string = "INSERT INTO clans (`banner`, `tag`, `leader_uuid`) VALUES (?,?,?);";
        try(Connection connection = database.getConnection();
            PreparedStatement insert = connection.prepareStatement(string))
        {
            insert.setString(1, data);
            insert.setString(2, tag);
            insert.setString(3, uuid.toString());
            if(insert.executeUpdate() != 0) {
                clans.put(tag.toLowerCase(), uuid);
                return this.addOfflinePlayerToClan(player, tag.toLowerCase());
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create clan: " + tag);
        }
        return false;
    }

    /**
     * Deletes a clan.
     *
     * @param tag
     * @return boolean
     */
    public boolean deleteClan(String tag) {
        String string = "DELETE FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement delete = connection.prepareStatement(string))
        {
            delete.setString(1, tag.toLowerCase(Locale.ROOT));
            if(delete.executeUpdate() != 0) {
                players.entrySet().removeIf(entry -> entry.getValue().equals(tag));
                clans.remove(tag.toLowerCase());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete clan: " + tag);
        }
        return false;
    }


    /**
     * Resigns a player from a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean resignOfflinePlayer(OfflinePlayer player) {
        String string = "DELETE FROM clan_members WHERE `mojang_uuid` = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement delete = connection.prepareStatement(string))
        {
            delete.setString(1, player.getUniqueId().toString());
            int rows = delete.executeUpdate();
            if(rows != 0) {
                players.remove(player.getUniqueId());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to resign player: " + player.getName());
        }
        return false;
    }

    /**
     * Sets a clan home for a given clan.
     *
     * @param tag
     * @param location
     * @return boolean
     */
    public boolean setClanHome(String tag, Location location) {
        String string = "UPDATE clans SET `home` = ? WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setString(1, SerializationUtil.locationToString(location));
            update.setString(2, tag.toLowerCase());
            if(update.executeUpdate() != 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to set clan home for clan: " + tag);
        }
        return false;
    }

    /**
     * Adds an offline player to a clan.
     *
     * @param player
     * @param tag
     */
    public boolean addOfflinePlayerToClan(OfflinePlayer player, String tag) {
        String query = "SELECT `id` FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement select = connection.prepareStatement(query))
        {
            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();

            if(results != null && results.next()) {

                int id = results.getInt("id");

                if(id != 0) {

                    String string = "INSERT INTO clan_members (`mojang_uuid`, `clan_id`) VALUES (?,?);";

                    try(
                            PreparedStatement insert = connection.prepareStatement(string))
                    {
                        insert.setString(1, player.getUniqueId().toString());
                        insert.setInt(2, id);
                        if(insert.executeUpdate() != 0) {
                            players.put(player.getUniqueId(), tag.toLowerCase());
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Failed to add offline player to clan: " + player.getName() + ", clan: " + tag);
                    }
                } else {
                    plugin.getLogger().severe("Clan ID result is 0.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve clan ID in add offline player.");
        }
        return false;
    }

    /**
     * Sets the clan leader to the provided player.
     *
     * @param tag
     * @param player
     * @return boolean
     */
    public boolean setClanLeader(String tag, OfflinePlayer player) {
        String string = "UPDATE clans SET `leader_uuid` = ? WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setString(1, player.getUniqueId().toString());
            update.setString(2, tag.toLowerCase());
            if(update.executeUpdate() != 0) {
                clans.put(tag.toLowerCase(), player.getUniqueId());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set clan leader for clan: " + tag + ", to:" + player.getName());
        }
        return false;
    }

    public UUID getLeader(String tag) {
        return clans.get(tag.toLowerCase());
    }

    public boolean promoteOfficer(OfflinePlayer offlinePlayer) {
        String string = "UPDATE clan_members SET `is_officer` = 1 WHERE `mojang_uuid` = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setString(1, offlinePlayer.getUniqueId().toString());
            if(update.executeUpdate() != 0) {
                officers.add(offlinePlayer.getUniqueId());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to promote " + offlinePlayer.getName() + " to officer role.");
        }
        return false;
    }

    public boolean demoteOfficer(OfflinePlayer offlinePlayer) {
        String string = "UPDATE clan_members SET `is_officer` = 0 WHERE `mojang_uuid` = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setString(1, offlinePlayer.getUniqueId().toString());
            if(update.executeUpdate() != 0) {
                officers.remove(offlinePlayer.getUniqueId());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to demote " + offlinePlayer.getName() + " from officer role.");
        }
        return false;
    }

    public boolean isOfflinePlayerOfficer(OfflinePlayer offlinePlayer) {
        return officers.contains(offlinePlayer.getUniqueId());
    }

    public boolean setOfflinePlayerActive(OfflinePlayer offlinePlayer, boolean b) {
        String string = "UPDATE clan_members SET `is_active` = ? WHERE `mojang_uuid` = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setBoolean(1, b);
            update.setString(2, offlinePlayer.getUniqueId().toString());
            if(update.executeUpdate() != 0) {
                if (b) activeMembers.add(offlinePlayer.getUniqueId());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning(e.toString());
            plugin.getLogger().severe("Failed to set " + offlinePlayer.getName() + " as active.");
        }
        return false;
    }

    public boolean isOfflinePlayerActive(OfflinePlayer offlinePlayer) {
        return activeMembers.contains(offlinePlayer.getUniqueId());
    }

    public List<OfflinePlayer> getClanOfficers(String clan) {
        return officers.stream()
                .filter(officerUUID -> isOfflinePlayerInSpecificClan(Bukkit.getOfflinePlayer(officerUUID),clan))
                .map(Bukkit::getOfflinePlayer)
                .collect(Collectors.toList());
    }

    public ItemStack getBanner(String tag) {
        String query = "SELECT `banner` FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement select = connection.prepareStatement(query)) {

            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();

            if(results != null && results.next()) {
                String banner = results.getString("banner");
                if(banner != null) {
                    return SerializationUtil.stringToStack(banner);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed check clan home for: " + tag);
        }
        return null;
    }

    /**
     * Adds a kill record to the clan_kills table.
     *
     * @param killer
     * @param killed
     */
    public boolean addKillRecord(OfflinePlayer killer, OfflinePlayer killed) {
        String query = "SELECT `id` FROM `clan_members` WHERE `mojang_uuid` = ?;";

        try(Connection connection = database.getConnection();
            PreparedStatement selectKiller = connection.prepareStatement(query);
            PreparedStatement selectKilled = connection.prepareStatement(query))
        {
            // Get killer clan_member id
            selectKiller.setString(1, killer.getUniqueId().toString());
            ResultSet killerResults = selectKiller.executeQuery();

            // Get killed clan_member id
            selectKilled.setString(1, killed.getUniqueId().toString());
            ResultSet killedResults = selectKilled.executeQuery();

            if(killerResults.next() && killedResults.next()) {

                String insertQuery = "INSERT INTO `clan_kills` (`killer_id`, `victim_id`) VALUES (?,?);";

                try(PreparedStatement insert = connection.prepareStatement(insertQuery)) {
                    insert.setInt(1, killerResults.getInt("id"));
                    insert.setInt(2, killedResults.getInt("id"));
                    if(insert.executeUpdate() != 0) {
                        return true;
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to add kill record: Killer: " + killer.getName() + ", Victim: " + killed.getName());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve Clan Member ID in addKillRecord.");
        }
        return false;
    }

    /**
     * Gets kill data within from database about a specific clan member.
     *
     * @param offlinePlayer
     */
    public Map<OfflinePlayer, Integer> getClanMemberKillData(OfflinePlayer offlinePlayer) {
        Map<OfflinePlayer, Integer> killData = new HashMap<>();

        String query = "SELECT cm.mojang_uuid, COUNT(*) AS kill_count " +
                "FROM `clan_kills` AS ck " +
                "INNER JOIN `clan_members` AS cm1 ON ck.`killer_id` = cm1.`id` " +
                "INNER JOIN `clan_members` AS cm ON ck.`victim_id` = cm.`id` " +
                "WHERE cm1.`mojang_uuid` = ? " +
                "AND ck.`date` > DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "GROUP BY cm.`mojang_uuid`;";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, offlinePlayer.getUniqueId().toString());
            statement.setInt(2, plugin.getFeatherClansConfig().getPVPScoreRelevantDays());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String victimUUIDString = resultSet.getString("mojang_uuid");
                    int killCount = resultSet.getInt("kill_count");

                    OfflinePlayer victim = Bukkit.getOfflinePlayer(UUID.fromString(victimUUIDString));
                    killData.put(victim, killCount);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve kill data for player: " + offlinePlayer.getName());
        }

        return killData;
    }

    public void updateLastSeenDate(OfflinePlayer offlinePlayer) {
        String sql = "UPDATE clan_members SET `last_seen_date` = CURRENT_TIMESTAMP WHERE `mojang_uuid` = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(sql)) {
            update.setString(1, offlinePlayer.getUniqueId().toString());
            update.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update last seen date for " + offlinePlayer.getName());
        }
    }


}
