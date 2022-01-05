package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.util.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanManager {

    private static HashMap<UUID, String> players = new HashMap<>();
    private static HashMap<String, UUID> clans = new HashMap<>();
    private final FeatherClans plugin;

    public ClanManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.load();
    }

    public void load() {

        List<ClanMember> players = ClanMember.findAll();
        players.stream().forEach(player -> {
            UUID uuid = UUID.fromString(player.getString("mojang_uuid"));
            String tag = player.parent(Clan.class).getString("tag");
            this.players.put(uuid, tag.toLowerCase());
        });

        List<Clan> clans = Clan.findAll();
        clans.stream().forEach(clan -> {
            String tag = clan.getString("tag");
            UUID leader = UUID.fromString(clan.getString("leader_uuid"));
            this.clans.put(tag.toLowerCase(), leader);
        });
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
                .filter(entry -> Objects.equals(entry.getValue(), clan.toLowerCase()))
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
        Clan clan = Clan.findFirst("tag = ?", tag);
        String data = clan.getString("home");
        return SerializationUtil.stringToLocation(data);
    }

    /**
     * Determines if an offline player is a member of a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean isOfflinePlayerInClan(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return ClanMember.findFirst("mojang_uuid = ?", uuid) != null;
    }

    public boolean isOfflinePlayerInSpecificClan(OfflinePlayer player, String clan) {
        String tag = players.get(player.getUniqueId());
        if (tag != null) {
            return tag.equals(clan);
        }
        return false;
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
        Clan clan = Clan.findFirst("tag = ?", tag);
        String data = clan.getString("home");
        return data != null;
    }

    /**
     * Creates a new clan.
     *
     * @param player leader
     * @param stack  banner
     * @param tag
     * @return returns the newly created clan or null if unsuccessful.
     */
    public Clan createClan(OfflinePlayer player, ItemStack stack, String tag) {

        UUID uuid = player.getUniqueId();

        ItemStack clone = stack.clone();
        clone.setAmount(1);
        String data = SerializationUtil.stackToString(clone);

        Clan clan = new Clan();
        clan.set("banner", data);
        clan.set("tag", tag);
        clan.set("leader_uuid", uuid.toString());

        if (clan.save()) {
            ClanMember member = new ClanMember();
            member.set("mojang_uuid", uuid.toString());
            clan.add(member);
            players.put(uuid, tag.toLowerCase());
            clans.put(tag.toLowerCase(), uuid);
            return clan;
        } else return null;
    }

    /**
     * Deletes a clan.
     *
     * @param tag
     * @return boolean
     */
    public boolean deleteClan(String tag) {
        Clan clan = Clan.findFirst("tag = ?", tag);
        boolean successful = clan.delete();
        if (successful) {
            clans.remove(tag.toLowerCase());
        }
        return successful;
    }


    /**
     * Resigns a player from a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean resignOfflinePlayer(OfflinePlayer player) {
        ClanMember member = ClanMember.findFirst("mojang_uuid = ?", player.getUniqueId().toString());
        if (member != null) {
            players.remove(player.getUniqueId());
            return member.delete();
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
        Clan clan = Clan.findFirst("tag = ?", tag);
        String data = SerializationUtil.locationToString(location);
        clan.set("home", data);
        return clan.save();
    }

    /**
     * Adds an offline player to a clan.
     *
     * @param player
     * @param tag
     */
    public void addOfflinePlayerToClan(OfflinePlayer player, String tag) {
        ClanMember member = new ClanMember();
        UUID uuid = player.getUniqueId();
        member.set("mojang_uuid", uuid.toString());

        Clan clan = Clan.findFirst("tag = ?", tag);
        players.put(player.getUniqueId(), clan.getString("tag").toLowerCase());

        clan.add(member);
    }

    /**
     * Sets the clan leader to the provided player.
     *
     * @param tag
     * @param player
     * @return boolean
     */
    public boolean setClanLeader(String tag, OfflinePlayer player) {
        Clan clan = Clan.findFirst("tag = ?", tag);
        clan.setString("leader_uuid", player.getUniqueId());
        boolean successful = clan.save();
        if (successful) {
            clans.put(tag.toLowerCase(), player.getUniqueId());
        }
        return successful;
    }

    public UUID getLeader(String tag) {
        return clans.get(tag.toLowerCase());
    }
}
