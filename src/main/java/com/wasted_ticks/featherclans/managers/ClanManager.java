package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.util.FeatherClansUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ClanManager {


    private final FeatherClans plugin;

    public ClanManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public List<Clan> getClans() {
        return Clan.findAll();
    }

    public Clan getClanByClanMember(ClanMember member) {
        return member.parent(Clan.class);
    }

    public List<ClanMember> getClanMembersByClan(Clan clan) {
        return clan.getAll(ClanMember.class);
    }

    public boolean isPlayerInClan(Player player) {
        UUID uuid = player.getUniqueId();
        return ClanMember.findFirst("mojang_uuid = ?", uuid) != null;
    }

    public Clan createClan(Player player, ItemStack stack, String tag) {

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

    public ClanMember getClanMemberByPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        return ClanMember.findFirst("mojang_uuid = ?", uuid.toString());
    }

    public boolean deleteClanMember(ClanMember member) {
        return member.delete();
    }

    public boolean isLeaderInClan(Player player) {
        UUID uuid = player.getUniqueId();
        return Clan.findFirst("leader_uuid = ?", uuid) != null;
    }

    public boolean setClanHome(Clan clan, Location location) {
        String data = FeatherClansUtil.locationToString(location);
        clan.set("home = ?", data);
        return clan.save();
    }

    public Location getClanHome(Clan clan) {
        String data = (String) clan.get("home");
        return FeatherClansUtil.stringToLocation(data);
    }

    public boolean hasClanHome(Clan clan) {
        String home = (String) clan.get("home");
        return home != null;
    }
}
