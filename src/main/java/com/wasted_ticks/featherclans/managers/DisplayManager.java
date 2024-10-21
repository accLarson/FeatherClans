package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Display;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisplayManager {

    private final FeatherClans plugin;
    private final List<Display> displays = new ArrayList<>();
    private Location originSignLocation;
    private BlockFace facing;
    private int increment;
    private int count;

    public DisplayManager(FeatherClans plugin) {
        this.plugin = plugin;
        if (this.plugin.getFeatherClansConfig().isDisplayEnabled()) this.init();
    }

    public void init() {
        if (validateSettings()) {
            this.increment = plugin.getFeatherClansConfig().getDisplayIncrement();
            this.count = plugin.getFeatherClansConfig().getDisplayCount();
            this.originSignLocation = new Location(
                    plugin.getServer().getWorlds().get(0),
                    plugin.getFeatherClansConfig().getDisplayOriginSignLocation().get(0),
                    plugin.getFeatherClansConfig().getDisplayOriginSignLocation().get(1),
                    plugin.getFeatherClansConfig().getDisplayOriginSignLocation().get(2));
            this.facing = BlockFace.valueOf(plugin.getFeatherClansConfig().getDisplayFacing());
            generateDisplays();
            updateDisplays();
        }
    }

    private void generateDisplays() {
        displays.clear();

        Location currentLoc = originSignLocation.clone();

        for (int i = 0; i < count; i++) {
            Location signLoc = currentLoc;
            signLoc.getBlock().setType(Material.AIR);
            Location standLoc = currentLoc.clone().add(0, 1, 0).add(facing.getOppositeFace().getDirection());
            clearArmorStand(standLoc);
            Location bannerLoc = currentLoc.clone().add(0, 4, 0).add(facing.getOppositeFace().getDirection());
            bannerLoc.getBlock().setType(Material.AIR);

            Sign sign = createSign(signLoc);
            ArmorStand stand = createArmorStand(standLoc);
            Banner banner = createBanner(bannerLoc);

            displays.add(new Display(banner, stand, sign));

            currentLoc = offset(currentLoc);
        }
    }

    private Sign createSign(Location location) {
        Block block = location.getBlock();
        block.setType(Material.OAK_WALL_SIGN);
        BlockState state = block.getState();
        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            Directional signData = (Directional) sign.getBlockData();
            signData.setFacing(facing);
            sign.setBlockData(signData);
            sign.update(true, false);
            return sign;
        }
        throw new IllegalStateException("Failed to create sign at " + location);
    }

    private ArmorStand createArmorStand(Location location) {
        // Center the ArmorStand on the block
        Location centeredLocation = location.clone().add(0.5, 0, 0.5);
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(centeredLocation, EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        armorStand.setRotation(getYawFromBlockFace(facing), 0);
        return armorStand;
    }

    private Banner createBanner(Location location) {
        Block block = location.getBlock();
        block.setType(Material.WHITE_WALL_BANNER);
        BlockState state = block.getState();
        if (state instanceof Banner) {
            Banner banner = (Banner) state;
            Directional bannerData = (Directional) banner.getBlockData();
            bannerData.setFacing(facing);
            banner.setBlockData(bannerData);
            banner.update(true, false);
            return banner;
        }
        throw new IllegalStateException("Failed to create banner at " + location);
    }



    private Location offset(Location location) {
        int offsetX = 0;
        int offsetZ = 0;

        switch (facing) {
            case NORTH:
                offsetX = -increment;
                break;
            case SOUTH:
                offsetX = increment;
                break;
            case EAST:
                offsetZ = -increment;
                break;
            case WEST:
                offsetZ = increment;
                break;
        }
        return location.clone().add(offsetX, 0, offsetZ);
    }

    private float getYawFromBlockFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return -180;
            case EAST:
                return -90;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            default:
                throw new IllegalArgumentException("Invalid BlockFace for setting armor stand display facing direction : " + face);
        }
    }

    private void clearArmorStand(Location location) {
        Location centeredLocation = location.clone().add(0.5, 0, 0.5);
        location.getWorld().getNearbyEntities(centeredLocation, 0.5, 0.5, 0.5).stream()
                .filter(entity -> entity instanceof ArmorStand)
                .filter(entity -> entity.getLocation().distanceSquared(centeredLocation) < 0.01)
                .forEach(Entity::remove);
    }

    private boolean validateSettings() {
        List<Integer> coords = plugin.getFeatherClansConfig().getDisplayOriginSignLocation();
        if (coords.size() != 3) {
            throw new IllegalArgumentException("Invalid origin sign location format. Use a list of 3 integers for x, y, and z");
        }
        if (!Arrays.asList("NORTH", "EAST", "SOUTH", "WEST").contains(plugin.getFeatherClansConfig().getDisplayFacing())) {
            throw new IllegalArgumentException("Facing direction must be NORTH, EAST, SOUTH, or WEST");
        }
        return true;
    }

    public void removeDisplaysInWorld() {
        for (Display display : displays) {
            display.getBanner().getBlock().setType(Material.AIR);
            display.getArmorStand().remove();
            display.getSign().getBlock().setType(Material.AIR);
        }
        displays.clear();
    }

    public void updateDisplays() {
        List<String> activeClans = plugin.getActivityManager().getActiveClans();

        for (int i = 0; i < displays.size(); i++) {
            final int index = i;
            Display display = displays.get(i);
            if (i < activeClans.size()) {
                String clanTag = activeClans.get(i);
                int clanSize = plugin.getMembershipManager().getClanSize(clanTag);
                int activeMemberCount = plugin.getActivityManager().getActiveMemberCount(clanTag);
                String leaderName = Bukkit.getOfflinePlayer(plugin.getClanManager().getClanLeader(clanTag)).getName();

                // Update sign
                display.updateSign(clanTag, clanSize, activeMemberCount, leaderName != null ? leaderName : "");

                // Update armor stand
                display.updateArmorStand(plugin.getClanManager().getClanLeader(clanTag));

                // Update banner
                display.updateBanner(clanTag, plugin.getClanManager());

                // Add a small delay before the next update
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (index < displays.size() - 1) {
                        updateDisplays();
                    }
                }, 5L); // 5 tick delay (0.25 seconds)
            } else {
                // Clear display if no clan data
                display.clearSign();
                display.clearStand();
                display.clearBanner();
            }
        }
    }
}
