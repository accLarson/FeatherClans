package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.util.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanManager {


    private final FeatherClans plugin;

    public ClanManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets a list of all clans.
     *
     * @return list of clans
     */
    public List<Clan> getClans() {
        return Clan.findAll();
    }

    /**
     * Gets the clan associated with the offline player.
     *
     * @param player
     * @return clan, will return null if offline player is not a member of a clan.
     */
    public Clan getClanByOfflinePlayer(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        ClanMember member = ClanMember.findFirst("mojang_uuid = ?", uuid.toString());
        if(member != null) {
            return member.parent(Clan.class);
        } else return null;
    }

    /**
     * Gets a list of offline players associated with a clan.
     *
     * @param clan
     * @return a list of offline players.
     */
    public List<OfflinePlayer> getOfflinePlayersByClan(Clan clan) {
        List<OfflinePlayer> players = clan.getAll(ClanMember.class).stream().map(member -> {
            UUID uuid = UUID.fromString(member.getString("mojang_uuid"));
            return Bukkit.getOfflinePlayer(uuid);
        }).collect(Collectors.toList());
        return players;
    }

    /**
     * Gets the clan home location for a given clan.
     *
     * @param clan
     * @return location
     */
    public Location getClanHome(Clan clan) {
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

    public boolean isOfflinePlayerInSpecificClan(OfflinePlayer player, Clan clan) {
        List<OfflinePlayer> players = this.getOfflinePlayersByClan(clan);
        return players.stream().map(p -> p.getUniqueId()).collect(Collectors.toList()).contains(player.getUniqueId());
    }


    /**
     * Determines if an offline player is a leader of a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean isOfflinePlayerLeader(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return Clan.findFirst("leader_uuid = ?", uuid) != null;
    }

    /**
     * Determines if a given clan has a home.
     *
     * @param clan
     * @return boolean
     */
    public boolean hasClanHome(Clan clan) {
        String home = clan.getString("home");
        return home != null;
    }

    /**
     * Creates a new clan.
     *
     * @param player leader
     * @param stack banner
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

        if(clan.save()) {
            ClanMember member = new ClanMember();
            member.set("mojang_uuid",uuid.toString());
            clan.add(member);
            return clan;
        } else return null;
    }

    /**
     * Deletes a clan.
     *
     * @param clan
     * @return boolean
     */
    public boolean deleteClan(Clan clan) {
        return clan.delete();
    }


    /**
     * Resigns a player from a clan.
     *
     * @param player
     * @return boolean
     */
    public boolean resignOfflinePlayer(OfflinePlayer player) {
        ClanMember member = ClanMember.findFirst("mojang_uuid = ?", player.getUniqueId().toString());
        if(member != null) {
            return member.delete();
        } return false;
    }

    /**
     * Sets a clan home for a given clan.
     *
     * @param clan
     * @param location
     * @return boolean
     */
    public boolean setClanHome(Clan clan, Location location) {
        String data = SerializationUtil.locationToString(location);
        clan.set("home", data);
        return clan.save();
    }

    /**
     * Adds an offline player to a clan.
     *
     * @param player
     * @param clan
     */
    public void addOfflinePlayerToClan(OfflinePlayer player, Clan clan) {
        ClanMember member = new ClanMember();
        UUID uuid = player.getUniqueId();
        member.set("mojang_uuid",uuid.toString());
        clan.add(member);
    }

    public boolean setClanLeader(Clan clan, OfflinePlayer player) {
        clan.setString("leader_uuid", player.getUniqueId());
        return clan.save();
    }

}
