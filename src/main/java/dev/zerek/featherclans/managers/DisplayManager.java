package dev.zerek.featherclans.managers;

import dev.zerek.featherclans.FeatherClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Banner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.BannerMeta;
import java.util.List;

import static org.bukkit.entity.EntityType.ARMOR_STAND;

public class DisplayManager {
    private final FeatherClans plugin;

    private Location displayLocation;
    private BlockFace blockFace;
    private int count;
    private double xIncrement = 0;
    private double zIncrement = 0;
    private float yaw = 45.0F;
    
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

    public void resetDisplays() {
        // Delete existing armor stands
        this.removeExistingDisplays();
        this.createDisplays();
    }

    private void removeExistingDisplays() {
        Location currentLocation = displayLocation.clone();

        int i = 0;

        while (i < count) {
            currentLocation.getChunk().load();
            currentLocation.getWorld().getNearbyEntities(currentLocation, 0.1, 0.1, 0.1)
                    .stream()
                    .filter(entity -> entity instanceof ArmorStand).findFirst().ifPresent(Entity::remove);
            
            Location bannerLocation = currentLocation.clone().add(0, 3, 0);
            if (bannerLocation.getBlock().getType().name().contains("BANNER")) {
                bannerLocation.getBlock().setType(Material.AIR);
            }
            
            // Remove sign if it exists
            Location signLocation = currentLocation.clone().add(0, -1, 0).add(blockFace.getModX(), 0, blockFace.getModZ());
            if (signLocation.getBlock().getType().name().contains("SIGN")) {
                signLocation.getBlock().setType(Material.AIR);
            }
            
           currentLocation.add(this.xIncrement, 0, this.zIncrement);
           i++;
        }
    }

    private void createDisplays() {
        Location currentLocation = displayLocation.clone();
        currentLocation.setPitch(0);
        currentLocation.setYaw(yaw);
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
            skullMeta.displayName(Component.text(String.valueOf(leader.getUniqueId())));
            head.setItemMeta(skullMeta);

            // Get clan armor with fallbacks for null items
            ItemStack chestplate = plugin.getClanManager().getChestplate(clanTag) != null ? plugin.getClanManager().getChestplate(clanTag) : DEFAULT_CHESTPLATE;
            ItemStack leggings = plugin.getClanManager().getLeggings(clanTag) != null ? plugin.getClanManager().getLeggings(clanTag) : DEFAULT_LEGGINGS;
            ItemStack boots = plugin.getClanManager().getBoots(clanTag) != null ? plugin.getClanManager().getBoots(clanTag) : DEFAULT_BOOTS;

            armorStand.setItem(EquipmentSlot.CHEST, chestplate);
            armorStand.setItem(EquipmentSlot.LEGS, leggings);
            armorStand.setItem(EquipmentSlot.FEET, boots);

            // set banner
            Location bannerLocation = currentLocation.clone().add(0, 3, 0);
            bannerLocation.setYaw(yaw);
            ItemStack bannerItem = plugin.getClanManager().getBanner(clanTag);

            if (bannerItem != null && bannerItem.getItemMeta() instanceof BannerMeta) {
                BannerMeta bannerMeta = (BannerMeta) bannerItem.getItemMeta();
                // Set the block to the banner material type
                String bannerType = bannerItem.getType().toString().replace("BANNER","WALL_BANNER");
                bannerLocation.getBlock().setType(Material.valueOf(bannerType));

                Directional directional = (Directional) bannerLocation.getBlock().getBlockData();
                directional.setFacing(blockFace);
                bannerLocation.getBlock().setBlockData(directional);
                
                // Get the block state and update it with the banner patterns
                Banner bannerState = (Banner) bannerLocation.getBlock().getState();
                bannerState.setPatterns(bannerMeta.getPatterns());
                bannerState.update();
            }
            
            // set sign
            Location signLocation = currentLocation.clone().add(0, -1, 0).add(blockFace.getModX(), 0, blockFace.getModZ());
            String configSignType = plugin.getFeatherClansConfig().getSignType();
            Material signMaterial;
            try {
                signMaterial = Material.valueOf(configSignType.toUpperCase() + "_WALL_SIGN");
            } catch (IllegalArgumentException e) {
                plugin.getLog().warning("Invalid sign_type in config: " + configSignType + ". Defaulting to OAK_WALL_SIGN");
                signMaterial = Material.OAK_WALL_SIGN;
            }
            signLocation.getBlock().setType(signMaterial);
            
            // Set the sign's facing direction
            Directional signData = (Directional) signLocation.getBlock().getBlockData();
            signData.setFacing(blockFace);
            signLocation.getBlock().setBlockData(signData);

            // Set the text on the sign
            Sign sign = (Sign) signLocation.getBlock().getState();
            String coloredTag = plugin.getClanManager().getColorTag(clanTag);
            String tagText = (coloredTag != null) ? coloredTag : clanTag;
            sign.getSide(Side.FRONT).line(0, MiniMessage.miniMessage().deserialize("<white>" + tagText));
            sign.getSide(Side.FRONT).line(2, MiniMessage.miniMessage().deserialize("<gray>" + leader.getName()));
            sign.getSide(Side.FRONT).line(3, MiniMessage.miniMessage().deserialize("<gray>Active: <#7FD47F>" + plugin.getActiveManager().getActiveMemberCount(clanTag)));
            sign.update();
            
            // Move to the next position
            currentLocation.add(this.xIncrement, 0, this.zIncrement);

           i++;
        }
    }
}
