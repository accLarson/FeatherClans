package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.utilities.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanManager {

    private final FeatherClans plugin;
    private static final HashMap<String, UUID> clans = new HashMap<>();
    private static final Map<String, String> partnerships = new HashMap<>();

    private final DatabaseManager database;

    public ClanManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseManager();
        this.load();
    }

    private void load() {
        loadClans();
        loadDisplays();
    }

    private void loadClans() {
        String string = "SELECT `tag`, `leader_uuid`, partner_id FROM clans;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(string);
            ResultSet results = statement.executeQuery())
        {
            if(results != null) {
                while (results.next()) {
                    String tag = results.getString("tag");
                    String uuid = results.getString("leader_uuid");
                    int partnerId = results.getInt("partner_id");
                    if(tag != null && uuid != null) {
                        clans.put(tag.toLowerCase(), UUID.fromString(uuid));

                        // LOAD PARTNERSHIPS
                        if (partnerId != -1) {
                            partnerships.put(tag,this.getTagById(partnerId));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load clans.");
        } catch(IllegalArgumentException e) {
            plugin.getLogger().severe("Failed to parse UUID into clan cache.");
        }
    }

    private void loadDisplays() {
        String string = "SELECT `banner`, `armorstand`, `sign` FROM clan_displays;";
        try(Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(string);
            ResultSet results = statement.executeQuery())
        {
            if(results != null) {
                while (results.next()) {
                    Sign sign = SerializationUtil.stringToSignBlock(results.getString("sign"));

                    if(sign != null) {
                        plugin.getDisplayManager().storeDisplayInMemory(null, null, sign);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to load displays.");
        }
    }

    public List<String> getClans() {
        return List.copyOf(clans.keySet());
    }

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

    public boolean isOfflinePlayerLeader(OfflinePlayer player) {
        return clans.containsValue(player.getUniqueId());
    }

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

    public int getClanSize(String tag, boolean activeOnly) {
        String query = activeOnly ?
                "SELECT COUNT(*) FROM clan_members cm JOIN clans c ON cm.clan_id = c.id WHERE c.tag = ? AND cm.is_active = true;" :
                "SELECT COUNT(*) FROM clan_members cm JOIN clans c ON cm.clan_id = c.id WHERE c.tag = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tag);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get clan size for: " + tag);
        }
        return 0;
    }

    public ItemStack getBanner(String tag) {
        // Implement the logic to retrieve the clan banner
        // This might involve querying the database or using cached data
        // Return the ItemStack representing the clan banner
    }

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
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create clan: " + tag);
        }
        return false;
    }

    public boolean deleteClan(String tag) {
        String string = "DELETE FROM clans WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement delete = connection.prepareStatement(string))
        {
            delete.setString(1, tag.toLowerCase());
            if(delete.executeUpdate() != 0) {
                clans.remove(tag.toLowerCase());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete clan: " + tag);
        }
        return false;
    }

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

    public int getClanIdByClan(String tag) {
        String query = "SELECT `id` FROM clans WHERE lower(tag) = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement select = connection.prepareStatement(query)) {

            select.setString(1, tag.toLowerCase());
            ResultSet results = select.executeQuery();

            if (results.next()) {
                return results.getInt("id");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve clan ID for clan: " + tag);
        }
        return -1;
    }

    public String getTagById(int id) {
        String query = "SELECT `tag` FROM clans WHERE id = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement select = connection.prepareStatement(query)) {

            select.setInt(1, id);
            ResultSet results = select.executeQuery();

            if (results.next()) {
                return results.getString("tag");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve clan tag for id: " + id);
        }
        return null;
    }

    public boolean hasPartner(String tag) {
        return partnerships.containsKey(tag);
    }

    public String getPartner(String tag) {
        return partnerships.get(tag);
    }

    public boolean setPartnership(String tag1, String tag2) {
        partnerships.put(tag2.toLowerCase(), tag1.toLowerCase());


        String string = "UPDATE clans SET `partner_id` = ? WHERE lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setInt(1, getClanIdByClan(tag2));
            update.setString(2, tag1.toLowerCase());
            if(update.executeUpdate() != 0) {
                partnerships.put(tag1.toLowerCase(), tag2.toLowerCase());
            }
            update.setInt(1, getClanIdByClan(tag1));
            update.setString(2, tag2.toLowerCase());
            if(update.executeUpdate() != 0) {
                partnerships.put(tag2.toLowerCase(), tag1.toLowerCase());
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to setup partnership: " + tag1 + ", and " + tag2);
        }
        return false;
    }

    public boolean createDisplayRecord(Sign sign) {
        String signString = SerializationUtil.signBlockToString(sign);

        String string = "INSERT INTO clan_displays (`sign`) VALUES (?);";
        try(Connection connection = database.getConnection();
            PreparedStatement insert = connection.prepareStatement(string))
        {
            insert.setString(1, signString);

            if(insert.executeUpdate() != 0) return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add display");
        }
        return false;
    }

    public boolean deleteDisplays() {
        String string = "DELETE FROM clan_displays;";
        try(Connection connection = database.getConnection();
            PreparedStatement delete = connection.prepareStatement(string))
        {
            if(delete.executeUpdate() != 0) return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete displays");
        }
        return false;
    }

    public Map<OfflinePlayer, Integer> getClanMemberKillData(OfflinePlayer player) {
        // Implement logic to retrieve kill data for the player
        return new HashMap<>(); // Placeholder implementation
    }

    public boolean resignOfflinePlayer(OfflinePlayer player) {
        String tag = plugin.getMembershipManager().getClanByOfflinePlayer(player);
        if (tag == null) {
            return false;
        }
        
        if (isOfflinePlayerLeader(player)) {
            return false;
        }
        
        boolean removed = plugin.getMembershipManager().removeOfflinePlayerFromClan(player);
        if (removed) {
            plugin.getActivityManager().updateClanActiveStatus(tag);
            return true;
        }
        
        return false;
    }
}
