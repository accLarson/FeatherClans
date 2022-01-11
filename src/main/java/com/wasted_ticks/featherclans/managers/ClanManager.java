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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanManager {

    private static HashMap<UUID, String> players = new HashMap<>();
    private static HashMap<String, UUID> clans = new HashMap<>();
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
    }

    private void loadPlayers() {
        String string = "SELECT mojang_uuid, c.tag  FROM clan_members AS cm left JOIN clans AS c ON c.id = cm.id;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(string);
            ResultSet results = statement.executeQuery())
        {
            if(results != null) {
                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("mojang_uuid"));
                    String tag = results.getString("tag");
                    players.put(uuid, tag.toLowerCase());
                }
            }
            plugin.getLog().info("[FeatherClans] Loaded players: " + players.keySet());
        } catch (SQLException e) {
            plugin.getLog().info("[FeatherClans] Failed to load players.");
        } catch(IllegalArgumentException e) {
            plugin.getLog().severe("[FeatherClans] Failed to parse UUID into player cache.");
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
                    UUID uuid = UUID.fromString(results.getString("leader_uuid"));
                    clans.put(tag.toLowerCase(), uuid);
                }
            }
            plugin.getLog().info("[FeatherClans] Loaded clans: " + clans.keySet());
        } catch (SQLException e) {
            plugin.getLog().info("[FeatherClans] Failed to load clans.");
        } catch(IllegalArgumentException e) {
            plugin.getLog().severe("[FeatherClans] Failed to parse UUID into clan cache.");
        }
    }

    public String getCachedClan(OfflinePlayer player) {
        return players.get(player.getUniqueId());
    }

    /**
     * Gets a list of all clans.
     *
     * @return list of clans
     */
    public List<String> getClans() {
        plugin.getLog().severe("Clans: " + clans.keySet());
        return List.copyOf(clans.keySet());
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
                .filter(entry -> entry.getValue().equalsIgnoreCase(clan))
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
                return SerializationUtil.stringToLocation(home);
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherEconomy] Failed check clan home for: " + tag);
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
        return players.keySet().contains(player.getUniqueId());
    }

    public boolean isOfflinePlayerInSpecificClan(OfflinePlayer player, String clan) {
        return (players.get(player.getUniqueId()) != null) ? players.get(player.getUniqueId()).equalsIgnoreCase(clan) : false;
    }


    /**
     * Determines if an offline player is a leader of a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean isOfflinePlayerLeader(OfflinePlayer player) {
        return clans.values().contains(player.getUniqueId());
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
                return true;
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherEconomy] Failed check clan home for: " + tag);
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
                return this.addOfflinePlayerToClan(player, tag);
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherEconomy] Failed to create clan: " + tag);
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
                return true;
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherEconomy] Failed to delete clan: " + tag);
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
            if(delete.executeUpdate() != 0) {
                return true;
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherEconomy] Failed to resign player: " + player.getName());
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
            plugin.getLog().severe("[FeatherEconomy] Failed to set clan home for clan: " + tag);
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
                    plugin.getLog().severe("[FeatherEconomy] Failed to add offline player to clan: " + player.getName() + ", clan: " + tag);
                }
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherEconomy] Failed to retrieve clan ID in add offline player.");
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
            plugin.getLog().severe("[FeatherEconomy] Failed to set clan leader for clan: " + tag + ", to:" + player.getName());
        }
        return false;
    }

    public UUID getLeader(String tag) {
        return clans.get(tag.toLowerCase());
    }
}
