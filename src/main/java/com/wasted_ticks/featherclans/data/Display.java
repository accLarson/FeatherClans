package com.wasted_ticks.featherclans.data;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.ActivityManager;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.managers.MembershipManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Display {

    private final Banner banner;
    private final ArmorStand armorStand;
    private final Sign sign;

    public Display(Banner banner, ArmorStand armorStand, Sign sign) {
        this.banner = banner;
        this.armorStand = armorStand;
        this.sign = sign;
    }

    public Banner getBanner() {
        return banner;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Sign getSign() {
        return sign;
    }

    public void updateSign(String clanTag, int clanSize, int activeMemberCount, String leaderName) {
        List<Component> signLines = new ArrayList<>();
        signLines.add(Component.text(clanTag));
        signLines.add(Component.text("Active: " + activeMemberCount + "/" + clanSize));
        signLines.add(Component.empty());
        signLines.add(Component.text(leaderName));

        for (int i = 0; i < 4; i++) {
            sign.getSide(Side.FRONT).line(i, signLines.get(i));
        }
        sign.update(true);
    }

    public void updateArmorStand(UUID leaderUUID) {
        ItemStack leaderHead = createPlayerHead(leaderUUID);
        
        armorStand.setCustomNameVisible(true);
        
        // Set grey dyed leather armor
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(Color.GRAY);
        chestplate.setItemMeta(meta);
        leggings.setItemMeta(meta);
        boots.setItemMeta(meta);
        
        armorStand.getEquipment().setChestplate(chestplate);
        armorStand.getEquipment().setLeggings(leggings);
        armorStand.getEquipment().setBoots(boots);
        armorStand.getEquipment().setHelmet(leaderHead);
    }

    public void updateBanner(String clanTag, ClanManager clanManager) {
        ItemStack bannerItem = clanManager.getBanner(clanTag);
        BannerMeta bannerMeta = (BannerMeta) bannerItem.getItemMeta();

        banner.setType(bannerItem.getType());
        banner.setPatterns(bannerMeta.getPatterns());

        banner.update(true, false);
    }

    public void clearSign() {
        for (int i = 0; i < 4; i++) {
            sign.getSide(Side.FRONT).line(i, Component.empty());
        }
        sign.update(true);
    }

    public void clearStand() {
        armorStand.setCustomName(null);
        armorStand.setCustomNameVisible(false);
        
        // Set white dyed leather armor, no helmet
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(Color.WHITE);
        chestplate.setItemMeta(meta);
        leggings.setItemMeta(meta);
        boots.setItemMeta(meta);
        
        armorStand.getEquipment().setChestplate(chestplate);
        armorStand.getEquipment().setLeggings(leggings);
        armorStand.getEquipment().setBoots(boots);
        armorStand.getEquipment().setHelmet(null);
    }

    public void clearBanner() {
        banner.setPatterns(new ArrayList<>());
        banner.setType(Material.WHITE_WALL_BANNER);
        banner.update(true, false);
    }

    private ItemStack createPlayerHead(UUID playerUUID) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        PlayerProfile profile = Bukkit.createPlayerProfile(playerUUID);
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        profile.update();
        
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);
        
        return head;
    }
}