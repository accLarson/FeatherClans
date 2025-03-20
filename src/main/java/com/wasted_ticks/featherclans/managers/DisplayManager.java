package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

import static org.bukkit.entity.EntityType.ARMOR_STAND;

public class DisplayManager {
    private final FeatherClans plugin;

    private Location displayLocation;
    private BlockFace blockFace;
    private int count;
    private double xIncrement = 0;
    private double zIncrement = 0;
    private double yaw = 45.0;
    
    // Default armor items for when clan-specific armor is null
    private static final ItemStack DEFAULT_CHESTPLATE = new ItemStack(Material.LEATHER_CHESTPLATE);
    private static final ItemStack DEFAULT_LEGGINGS = new ItemStack(Material.LEATHER_LEGGINGS);
    private static final ItemStack DEFAULT_BOOTS = new ItemStack(Material.LEATHER_BOOTS);

    public DisplayManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.init();
    }

    private void init() {
        this.displayLocation = plugin.getFeatherClansConfig().getDisplayLocation();
        this.blockFace = BlockFace.valueOf(plugin.getFeatherClansConfig().getDisplayFacing());
        this.count = plugin.getFeatherClansConfig().getDisplayCount();

        if (blockFace == BlockFace.NORTH) {
            this.yaw = -180.0f;
            this.xIncrement = -1; // Move west
        }
        else if (blockFace == BlockFace.EAST) {
            this.yaw = -90.0f;
            this.zIncrement = -1; // Move north
        }
        else if (blockFace == BlockFace.SOUTH) {
            this.yaw = 0.0f;
            this.xIncrement = 1; // Move east
        }
        else if (blockFace == BlockFace.WEST) {
            this.yaw = 90.0f;
            this.zIncrement = 1; // Move south
        }

        resetDisplays();
    }

    private void resetDisplays() {
        // Delete existing armor stands
        this.deleteExistingArmorStands();
        this.createArmorStands();
    }

    private void deleteExistingArmorStands() {
        Location currentLocation = displayLocation.clone();

        int i = 0;

        while (i < count) {
            currentLocation.getChunk().load();
            currentLocation.getWorld().getNearbyEntities(currentLocation, 0.1, 0.1, 0.1)
                    .stream()
                    .filter(entity -> entity instanceof ArmorStand).findFirst().ifPresent(Entity::remove);
            currentLocation.add(this.xIncrement, 0, this.zIncrement);
            i++;
        }
    }

    private void createArmorStands() {
        Location currentLocation = displayLocation.clone();
        currentLocation.setPitch(0);
        currentLocation.setYaw((float) yaw);
        List<String> activeClans = plugin.getActiveManager().getActiveClansOrdered();

        int i = 0;
        int j = Math.min(activeClans.size(), count);

        while (i < j) {
            String clanTag = activeClans.get(i);
            currentLocation.getChunk().load();

            // Create armor stand at the current location
            ArmorStand armorStand = (ArmorStand) currentLocation.getWorld().spawnEntity(currentLocation, ARMOR_STAND);

            // Configure the armor stand
            armorStand.setVisible(true);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);


            // set head
            OfflinePlayer leader = Bukkit.getOfflinePlayer(plugin.getClanManager().getLeader(clanTag));
            armorStand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.PLAYER_HEAD));
            ItemStack head = armorStand.getItem(EquipmentSlot.HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwningPlayer(leader);
//            skullMeta.displayName(Component.text(leader.getName()));
            head.setItemMeta(skullMeta);

            // Get clan armor with fallbacks for null items
            ItemStack chestplate = plugin.getClanManager().getChestplate(clanTag) != null ? plugin.getClanManager().getChestplate(clanTag) : DEFAULT_CHESTPLATE;
            ItemStack leggings = plugin.getClanManager().getLeggings(clanTag) != null ? plugin.getClanManager().getLeggings(clanTag) : DEFAULT_LEGGINGS;
            ItemStack boots = plugin.getClanManager().getBoots(clanTag) != null ? plugin.getClanManager().getBoots(clanTag) : DEFAULT_BOOTS;

            armorStand.setItem(EquipmentSlot.CHEST, chestplate);
            armorStand.setItem(EquipmentSlot.LEGS, leggings);
            armorStand.setItem(EquipmentSlot.FEET, boots);

            // Move to the next position
            currentLocation.add(this.xIncrement, 0, this.zIncrement);

            i++;
        }
    }
}
