package com.wasted_ticks.featherclans.data;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.ClanManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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

        createPlayerHead(leaderUUID, leaderHead -> {
            armorStand.getEquipment().setHelmet(leaderHead);
        });



    }

    public void updateBanner(String clanTag, ClanManager clanManager) {
        ItemStack bannerItem = clanManager.getBanner(clanTag);
        BannerMeta bannerMeta = (BannerMeta) bannerItem.getItemMeta();

        BlockData originalBlockData = banner.getBlockData();

        banner.setType(bannerItem.getType());
        banner.setPatterns(bannerMeta.getPatterns());

        banner.setBlockData(originalBlockData);

        banner.update(true, false);
    }

    public void clearSign() {
        for (int i = 0; i < 4; i++) {
            sign.getSide(Side.FRONT).line(i, Component.empty());
        }
        sign.update(true);
    }

    public void clearStand() {
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

    private void createPlayerHead(UUID playerUUID, Consumer<ItemStack> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(FeatherClans.getPlugin(FeatherClans.class), () -> {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            
            com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(playerUUID);
            boolean completed = profile.complete(true);
            
            if (completed) {
                meta.setPlayerProfile(profile);
                head.setItemMeta(meta);
                Bukkit.getScheduler().runTask(FeatherClans.getPlugin(FeatherClans.class), () -> callback.accept(head));
            } else {
                Bukkit.getLogger().warning("Failed to complete profile for UUID: " + playerUUID);
                Bukkit.getScheduler().runTask(FeatherClans.getPlugin(FeatherClans.class), () -> callback.accept(createDefaultHead()));
            }
        });
    }

    private ItemStack createDefaultHead() {
        return new ItemStack(Material.PLAYER_HEAD);
    }
}