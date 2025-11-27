package dev.zerek.featherclans.managers;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.utilities.SerializationUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class ClanManager {

    private static final Map<UUID, String> players = new HashMap<>();
    private static final Set<UUID> officers = new HashSet<>();
    private static final Map<String, UUID> clans = new HashMap<>();
    private static final Map<String, String> coloredTags = new HashMap<>();
    private static final Map<String, String> alliances = new HashMap<>();
    private final FeatherClans plugin;
    private final DatabaseManager database;

    public ClanManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
        this.load();
    }

    private void load() {
        loadPlayers();
        loadOfficers();
        loadClans();
        loadColoredTags();
        loadAlliances();
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
            plugin.getLogger().info("Loaded " + players.size() + " clan members into cache.");
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load players.");
        } catch(IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to parse UUID into player cache.");
        }
    }

    private void loadOfficers() {
        String string = "SELECT mojang_uuid FROM clan_members WHERE is_officer = true;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(string);
            ResultSet results = statement.executeQuery())
        {
            if(results != null) {
                while (results.next()) {
                    String uuid = results.getString("mojang_uuid");
                    if(uuid != null) {
                        try {
                            officers.add(UUID.fromString(uuid));
                        } catch(IllegalArgumentException e) {
                            plugin.getLogger().severe("Failed to parse UUID into officer cache: " + uuid);
                        }
                    }
                }
            }
            plugin.getLogger().info("Loaded " + officers.size() + " officers into cache.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load officers.");
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
            plugin.getLogger().info("Loaded " + clans.size() + " clans into cache.");
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load clans.");
        } catch(IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to parse UUID into clan cache.");
        }
    }

    private void loadColoredTags() {
        String query = "SELECT `tag`, `colored_tag` FROM clans WHERE colored_tag IS NOT NULL;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery())
        {
            if(results != null) {
                while (results.next()) {
                    String tag = results.getString("tag");
                    String coloredTag = results.getString("colored_tag");
                    if(tag != null && coloredTag != null) {
                        coloredTags.put(tag.toLowerCase(), coloredTag);
                    }
                }
            }
            plugin.getLogger().info("Loaded " + coloredTags.size() + " colored tags into cache.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load colored tags.");
        }
    }
    
    private void loadAlliances() {
        String query = "SELECT c1.tag as tag1, c2.tag as tag2 FROM clan_alliances " +
                      "JOIN clans c1 ON clan_alliances.clan_1 = c1.id " +
                      "JOIN clans c2 ON clan_alliances.clan_2 = c2.id;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery())
        {
            int count = 0;
            if(results != null) {
                while (results.next()) {
                    String clan1Tag = results.getString("tag1");
                    String clan2Tag = results.getString("tag2");
                    
                    // Store both directions of the alliance
                    alliances.put(clan1Tag.toLowerCase(), clan2Tag.toLowerCase());
                    alliances.put(clan2Tag.toLowerCase(), clan1Tag.toLowerCase());
                    count += 2;
                }
            }
            plugin.getLogger().info("Loaded " + (count/2) + " clan alliances (" + count + " mappings) into cache.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load clan alliances.");
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
                    return SerializationUtility.stringToLocation(home);
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

    public boolean isOfflinePlayerInSpecificClan(OfflinePlayer kickee, String clan) {
        return players.containsKey(kickee.getUniqueId()) && players.get(kickee.getUniqueId()).equalsIgnoreCase(clan);
    }
    
    public boolean isUsernameInSpecificClan(String username, String clan) {
        List<String> clanUsernames = this.getOfflinePlayersByClan(clan)
                .stream()
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return clanUsernames.contains(username.toLowerCase());
    }
    
    public UUID getUUIDFromUsername(String username) {
        for (UUID uuid : players.keySet()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String playerName = offlinePlayer.getName();
            if (playerName != null && playerName.equalsIgnoreCase(username)) {
                return uuid;
            }
        }
        return null;
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
     * Determines if an offline player is an officer in a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean isOfflinePlayerOfficer(OfflinePlayer player) {
        return officers.contains(player.getUniqueId());
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
        String data = SerializationUtility.stackToString(clone);
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
                boolean success = this.addOfflinePlayerToClan(player, tag.toLowerCase());
                if(success) plugin.getActiveManager().updateActiveStatus(player, tag.toLowerCase());
                return success;
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
        this.getOfficers(tag).forEach(officers::remove);
        
        // Remove clan from active tracking before deleting
        plugin.getActiveManager().removeClan(tag.toLowerCase());
        
        String string = "DELETE FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement delete = connection.prepareStatement(string))
        {
            delete.setString(1, tag.toLowerCase(Locale.ROOT));
            if(delete.executeUpdate() != 0) {
                players.entrySet().removeIf(entry -> entry.getValue().equals(tag));
                clans.remove(tag.toLowerCase());
                coloredTags.remove(tag.toLowerCase());
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
        String clanTag = players.get(player.getUniqueId());
        
        try(Connection connection = database.getConnection();
            PreparedStatement delete = connection.prepareStatement(string))
        {
            delete.setString(1, player.getUniqueId().toString());
            int rows = delete.executeUpdate();
            if(rows != 0) {
                // Remove from local caches
                players.remove(player.getUniqueId());
                officers.remove(player.getUniqueId());
                
                // Update active status for the clan they left
                if (clanTag != null) {
                    plugin.getActiveManager().removePlayerFromActive(player.getUniqueId(), clanTag);
                }
                
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
            update.setString(1, SerializationUtility.locationToString(location));
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
     * Sets the clan armor for a given clan.
     *
     * @param tag clan tag
     * @param chestplate chestplate item
     * @param leggings leggings item
     * @param boots boots item
     * @return boolean indicating success
     */
    public boolean setClanArmor(String tag, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        String query = "UPDATE clans SET chestplate = ?, leggings = ?, boots = ? WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(query)) {
            update.setString(1, SerializationUtility.stackToString(chestplate));
            update.setString(2, SerializationUtility.stackToString(leggings));
            update.setString(3, SerializationUtility.stackToString(boots));
            update.setString(4, tag.toLowerCase());
            return update.executeUpdate() != 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set clan armor for clan: " + tag);
        }
        return false;
    }

    /**
     * Sets the clan banner for a given clan.
     *
     * @param tag clan tag
     * @param banner banner item
     * @return boolean indicating success
     */
    public boolean setBanner(String tag, ItemStack banner) {
        String query = "UPDATE clans SET banner = ? WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(query)) {
            update.setString(1, SerializationUtility.stackToString(banner));
            update.setString(2, tag.toLowerCase());
            if(update.executeUpdate() != 0) {
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set clan banner for clan: " + tag);
        }
        return false;
    }

    /**
     * Gets the chestplate for a given clan.
     *
     * @param tag
     * @return ItemStack representing the clan's chestplate, or null if not found
     */
    public ItemStack getChestplate(String tag) {
        String query = "SELECT `chestplate` FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement select = connection.prepareStatement(query)) {

            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();

            if(results != null && results.next()) {
                String chestplate = results.getString("chestplate");
                if(chestplate !=  null) {
                    return SerializationUtility.stringToStack(chestplate);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get chestplate for clan: " + tag);
        }
        return null;
    }

    /**
     * Gets the leggings for a given clan.
     *
     * @param tag
     * @return ItemStack representing the clan's leggings, or null if not found
     */
    public ItemStack getLeggings(String tag) {
        String query = "SELECT `leggings` FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement select = connection.prepareStatement(query)) {

            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();

            if(results != null &&  results.next()) {
                String leggings = results.getString("leggings");
                if(leggings != null) {
                    return SerializationUtility.stringToStack(leggings);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get leggings for clan: " + tag);
        }
        return null;
    }

    /**
     * Gets the boots for a given clan.
     *
     * @param tag
     * @return ItemStack representing the clan's boots, or null if not found
     */
    public ItemStack getBoots(String tag) {
        String query = "SELECT `boots` FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement select = connection.prepareStatement(query)) {

            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();

            if(results != null && results.next()) {
                String boots = results.getString("boots");
                if(boots != null) {
                    return SerializationUtility.stringToStack(boots);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get boots for clan: " + tag);
        }
        return null;
    }

    /**
     * Gets the color tag for a given clan.
     *
     * @param tag clan tag
     * @return String representing the clan's color tag, or null if not found
     */
    public String getColorTag(String tag) {
        return coloredTags.get(tag.toLowerCase());
    }

    /**
     * Sets the color tag for a given clan.
     *
     * @param tag clan tag
     * @param coloredTag color tag value
     * @return boolean indicating success
     */
    public boolean setColorTag(String tag, String coloredTag) {
        String query = "UPDATE clans SET colored_tag = ? WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(query)) {
            update.setString(1, coloredTag);
            update.setString(2, tag.toLowerCase());
            if (update.executeUpdate() != 0) {
                coloredTags.put(tag.toLowerCase(), coloredTag);
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set color tag for clan: " + tag);
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

                    String string = "INSERT INTO clan_members (`mojang_uuid`, `clan_id`, `is_officer`) VALUES (?,?, ?);";

                    try(
                            PreparedStatement insert = connection.prepareStatement(string))
                    {
                        insert.setString(1, player.getUniqueId().toString());
                        insert.setInt(2, id);
                        insert.setBoolean(3, false);
                        if(insert.executeUpdate() != 0) {
                            // Update caches: set clan membership and ensure officer flag is cleared in-memory
                            players.put(player.getUniqueId(), tag.toLowerCase());
                            officers.remove(player.getUniqueId());
                            
                            // Update active status for the new member
                            plugin.getActiveManager().updateActiveStatus(player, tag.toLowerCase());
                            
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

    /**
     * Sets the clan officer status for the provided player.
     *
     * @param player
     * @param status
     * @return boolean
     */
    public boolean setClanOfficerStatus(OfflinePlayer player, boolean status) {
        String string = "UPDATE clan_members SET is_officer = ? WHERE mojang_uuid = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setBoolean(1, status);
            update.setString(2, player.getUniqueId().toString());
            if(update.executeUpdate() != 0) {
                if(status) officers.add(player.getUniqueId());
                else officers.remove(player.getUniqueId());
                
                plugin.getLogger().info("(Officer) Successfully " + (status ? "promoted" : "demoted") + " player: " + player.getName());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set officer status for player: " + player.getName());
        }
        return false;
    }
    
    public UUID getLeader(String tag) {
        return clans.get(tag.toLowerCase());
    }

    public List<UUID> getOfficers(String tag) {
        return officers.stream()
                .filter(officerUUID -> {
                    String playerClan = players.get(officerUUID);
                    return playerClan != null && playerClan.equalsIgnoreCase(tag);
                })
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
                    return SerializationUtility.stringToStack(banner);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get banner for: " + tag);
        }
        return null;
    }

    public boolean hasAlly(String clan) {
        return (alliances.containsKey(clan.toLowerCase()));
    }

    public String getAlly(String clan) {
        return alliances.get(clan);
    }

    private Integer getAllianceID(String clan) {
        String query = "SELECT id FROM clan_alliances WHERE " +
                "clan_1 = (SELECT id FROM clans WHERE lower(tag) = ?) OR " +
                "clan_2 = (SELECT id FROM clans WHERE lower(tag) = ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, clan.toLowerCase());
            statement.setString(2, clan.toLowerCase());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt("id");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get alliance ID for clan " + clan + ": " + e.getMessage());
        }
        return null;
    }

    public boolean addAlliance(String clan1, String clan2) {
        String query = "INSERT INTO clan_alliances (clan_1, clan_2) VALUES ((SELECT id FROM clans WHERE lower(tag) = ?), (SELECT id FROM clans WHERE lower(tag) = ?))";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, clan1.toLowerCase());
            statement.setString(2, clan2.toLowerCase());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                // Update the cache with both directions of the alliance
                alliances.put(clan1.toLowerCase(), clan2.toLowerCase());
                alliances.put(clan2.toLowerCase(), clan1.toLowerCase());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add alliance between " + clan1 + " and " + clan2 + ": " + e.getMessage());
        }
        return false;
    }

    public boolean removeAlliance(String clan1, String clan2) {
        Integer allianceID = getAllianceID(clan1);
        if (allianceID == null) {
            plugin.getLogger().warning("No alliance found for clan " + clan1);
            return false;
        }
        String query = "DELETE FROM clan_alliances WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, allianceID);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                alliances.remove(clan1.toLowerCase());
                alliances.remove(clan2.toLowerCase());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove alliance between " + clan1 + " and " + clan2 + ": " + e.getMessage());
        }
        return false;
    }
}
