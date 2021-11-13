package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.util.FeatherClansUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanManager {


    private final FeatherClans plugin;

    public ClanManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public List<Clan> getClans() {
        return Clan.findAll();
    }

    public Clan getClanByOfflinePlayer(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        ClanMember member = ClanMember.findFirst("mojang_uuid = ?", uuid.toString());
        return member.parent(Clan.class);
    }

    public List<OfflinePlayer> getOfflinePlayersByClan(Clan clan) {
        List<OfflinePlayer> players = clan.getAll(ClanMember.class).stream().map(member -> {
            UUID uuid = UUID.fromString(member.getString("mojang_uuid"));
            return Bukkit.getOfflinePlayer(uuid);
        }).collect(Collectors.toList());
        return players;
    }

    public Location getClanHome(Clan clan) {
        String data = (String) clan.get("home");
        return FeatherClansUtil.stringToLocation(data);
    }

    public boolean isOfflinePlayerInClan(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return ClanMember.findFirst("mojang_uuid = ?", uuid) != null;
    }

    public boolean isOfflinePlayerLeader(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return Clan.findFirst("leader_uuid = ?", uuid) != null;
    }

    public boolean hasClanHome(Clan clan) {
        String home = (String) clan.get("home");
        return home != null;
    }

    public Clan createClan(OfflinePlayer player, ItemStack stack, String tag) {

        UUID uuid = player.getUniqueId();

        ItemStack clone = stack.clone();
        clone.setAmount(1);
        String data = FeatherClansUtil.stackToString(clone);

        Clan clan = new Clan();
        clan.set("banner", data);
        clan.set("tag", tag);
        clan.set("leader_uuid", uuid.toString());
        clan.save();

        ClanMember member = new ClanMember();
        member.set("mojang_uuid",uuid.toString());
        clan.add(member);

        return clan;
    }

    public boolean deleteClan(Clan clan) {
        return clan.delete();
    }

    public boolean resignOfflinePlayer(OfflinePlayer player) {
        ClanMember member = ClanMember.findFirst("mojang_uuid = ?", player.getUniqueId().toString());
        return member.delete();
    }

    public boolean setClanHome(Clan clan, Location location) {
        String data = FeatherClansUtil.locationToString(location);
        clan.set("home", data);
        return clan.save();
    }

    public void addOfflinePlayerToClan(OfflinePlayer player, Clan clan) {
        ClanMember member = new ClanMember();
        UUID uuid = player.getUniqueId();
        member.set("mojang_uuid",uuid.toString());
        clan.add(member);
    }
}
