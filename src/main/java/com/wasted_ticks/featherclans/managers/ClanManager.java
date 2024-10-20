package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.utilities.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
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

    public List<String> getClans() {
        return List.copyOf(clans.keySet());
    }

    public void updateLeader(String tag, UUID newLeaderUUID) {
        clans.put(tag.toLowerCase(), newLeaderUUID);
    }

    public UUID getClanLeader(String tag) {
        return clans.get(tag);
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

    public ItemStack getBanner(String tag) {
        String query = "SELECT `banner` FROM clans WHERE lower(tag) = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tag.toLowerCase());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                String bannerData = result.getString("banner");
                if (bannerData != null) {
                    return SerializationUtil.stringToStack(bannerData);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve banner for clan: " + tag);
        }
        return null;
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

    public String getFormattedTagById(int id) {
        String query = "SELECT `tag` FROM clans WHERE id = ?;";
        try (Connection connection = database.getConnection();
             PreparedStatement select = connection.prepareStatement(query)) {

            select.setInt(1, id);
            ResultSet results = select.executeQuery();

            if (results.next()) {
                return results.getString("tag_color");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve formatted clan tag for id: " + id);
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

    public boolean removePartnership(String tag1, String tag2) {
        partnerships.remove(tag1.toLowerCase());
        partnerships.remove(tag2.toLowerCase());

        String string = "UPDATE clans SET `partner_id` = -1 WHERE lower(tag) = ? OR lower(tag) = ?;";
        try(Connection connection = database.getConnection();
            PreparedStatement update = connection.prepareStatement(string))
        {
            update.setString(1, tag1.toLowerCase());
            update.setString(2, tag2.toLowerCase());
            if(update.executeUpdate() != 0) {
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove partnership: " + tag1 + ", and " + tag2);
        }
        return false;
    }

    public boolean createDisplayRecord(Banner banner, ArmorStand armorStand, Sign sign) {
        String bannerString = SerializationUtil.bannerBlockToString(banner);
        String armorStandString = SerializationUtil.armorStandToString(armorStand);
        String signString = SerializationUtil.signBlockToString(sign);

        String string = "INSERT INTO clan_displays (`banner`, `armorstand`, `sign`) VALUES (?,?,?);";
        try(Connection connection = database.getConnection();
            PreparedStatement insert = connection.prepareStatement(string))
        {
            insert.setString(1, bannerString);
            insert.setString(2, armorStandString);
            insert.setString(3, signString);

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
}
