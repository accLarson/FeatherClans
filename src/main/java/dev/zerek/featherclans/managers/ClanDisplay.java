package dev.zerek.featherclans.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.zerek.featherclans.FeatherClans;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A single clan display slot: a player-skinned {@link Mannequin} standing on an anchor block, with
 * an auto-placed wall sign, an optional decorative hopper pedestal, and a wall banner floating above.
 *
 * Ported from FeatherQueenOfTheHill's DisplayManager, parameterized so a whole row of these can be
 * built from one anchor coordinate. Entities are non-persistent and the chunk(s) are held loaded via
 * plugin chunk tickets, so the display is rebuilt each load. A slot is "loaded" once its anchor, sign
 * and (optional) base are bound; the mannequin and banner are only present while the slot is occupied
 * by an active clan (see {@link #fill} / {@link #clear}).
 */
public class ClanDisplay {

    private final FeatherClans plugin;
    private final Location anchorBlockLocation;
    private final BlockFace facing;
    private final double scale;
    private final boolean showBase;
    private final int bannerHeight;
    private final Material signMaterial;

    private final List<long[]> heldChunks = new ArrayList<>();
    private Block anchorBlock;
    private Sign sign;
    private final List<BlockDisplay> baseDisplay = new ArrayList<>();
    private Mannequin mannequin;
    private boolean valid;
    private boolean keepSign;

    public ClanDisplay(FeatherClans plugin, Location anchorBlockLocation, BlockFace facing,
                       double scale, boolean showBase, int bannerHeight, Material signMaterial) {
        this.plugin = plugin;
        this.anchorBlockLocation = anchorBlockLocation;
        this.facing = facing;
        this.scale = scale;
        this.showBase = showBase;
        this.bannerHeight = bannerHeight;
        this.signMaterial = signMaterial;
    }

    /**
     * Holds the slot's chunk(s) loaded, binds/places the wall sign, and spawns the decorative base.
     * Does NOT spawn the mannequin — that happens on {@link #fill} once the slot has an active clan.
     */
    public CompletableFuture<Boolean> load() {
        World world = anchorBlockLocation.getWorld();
        int anchorChunkX = anchorBlockLocation.getBlockX() >> 4;
        int anchorChunkZ = anchorBlockLocation.getBlockZ() >> 4;
        holdChunk(world, anchorChunkX, anchorChunkZ);

        int signChunkX = (anchorBlockLocation.getBlockX() + facing.getModX()) >> 4;
        int signChunkZ = (anchorBlockLocation.getBlockZ() + facing.getModZ()) >> 4;
        if (signChunkX != anchorChunkX || signChunkZ != anchorChunkZ) {
            holdChunk(world, signChunkX, signChunkZ);
        }

        return world.getChunkAtAsync(anchorChunkX, anchorChunkZ).thenApply(chunk -> {
            if (!plugin.isEnabled()) return false;
            this.valid = validate();
            return this.valid;
        });
    }

    /** Checks the slot's location is usable WITHOUT placing anything: solid target, clear air column, free banner + sign slots. */
    private boolean validate() {
        this.anchorBlock = anchorBlockLocation.getBlock();
        // Target must be a solid block for the mannequin to stand on.
        if (!anchorBlock.getType().isSolid()) {
            plugin.getLogger().warning("Display anchor-block at " + coordString(anchorBlockLocation) + " is missing or not solid.");
            return false;
        }
        // The mannequin needs a clear (air) column between the anchor and the banner.
        for (int i = 1; i < bannerHeight; i++) {
            Location above = anchorBlockLocation.clone().add(0, i, 0);
            if (!above.getBlock().getType().isAir()) {
                plugin.getLogger().warning("Cannot place display anchored at " + coordString(anchorBlockLocation)
                        + " — block at " + coordString(above) + " must be air.");
                return false;
            }
        }
        // The banner slot must be air or already hold a banner (a prior run's banner persists as a real block).
        if (!isBannerSpaceClear()) {
            plugin.getLogger().warning("Cannot place banner at " + coordString(bannerLocation())
                    + " — block is occupied (must be air or a banner).");
            return false;
        }
        // The sign slot must be free or already hold a sign.
        Block signBlock = anchorBlock.getRelative(facing);
        if (!(signBlock.getState() instanceof Sign) && !signBlock.getType().isAir()) {
            plugin.getLogger().warning("Cannot place sign at " + coordString(signBlock.getLocation()) + " — block is occupied.");
            return false;
        }
        return true;
    }

    /** Places the persistent sign for a validated slot (the pedestal follows the mannequin via {@link #fill}/{@link #clear}). No-op if validation failed. */
    public void place() {
        if (!valid) return;
        bindOrPlaceSign();
    }

    private void holdChunk(World world, int chunkX, int chunkZ) {
        if (world.addPluginChunkTicket(chunkX, chunkZ, plugin)) {
            heldChunks.add(new long[]{chunkX, chunkZ});
        }
    }

    private boolean bindOrPlaceSign() {
        Block signBlock = anchorBlock.getRelative(facing);
        // Bind an existing sign only if it's already the configured type; otherwise (re)place it.
        if (signBlock.getState() instanceof Sign existing && signBlock.getType() == signMaterial) {
            this.sign = existing;
            return true;
        }
        if (!signBlock.getType().isAir() && !(signBlock.getState() instanceof Sign)) {
            plugin.getLogger().warning("Cannot place sign at " + coordString(signBlock.getLocation()) + " — block is occupied.");
            return false;
        }
        signBlock.setType(signMaterial, false);
        if (signBlock.getBlockData() instanceof WallSign wallSign) {
            wallSign.setFacing(facing);
            signBlock.setBlockData(wallSign, false);
        }
        this.sign = (Sign) signBlock.getState();
        return true;
    }

    private void spawnBaseDisplay() {
        double anchorTopY = anchorBlock.getBoundingBox().getMaxY();
        Location baseLoc = new Location(
                anchorBlockLocation.getWorld(),
                anchorBlockLocation.getBlockX(),
                anchorTopY,
                anchorBlockLocation.getBlockZ()
        );
        Location sweepCenter = baseLoc.clone().add(0.5, 0.25, 0.5);
        sweepCenter.getWorld().getNearbyEntitiesByType(BlockDisplay.class, sweepCenter, 0.5, 0.5, 0.5)
                .forEach(BlockDisplay::remove);
        boolean facingZAxis = facing == BlockFace.NORTH || facing == BlockFace.SOUTH;
        float scaleX = facingZAxis ? 0.8f : 0.5f;
        float scaleZ = facingZAxis ? 0.5f : 0.8f;
        float translateX = (1f - scaleX) / 2f;
        float translateZ = (1f + scaleZ) / 2f;
        baseDisplay.add(baseLoc.getWorld().spawn(baseLoc, BlockDisplay.class, d -> {
            d.setBlock(Material.HOPPER.createBlockData());
            d.setPersistent(false);
            d.setTransformation(new Transformation(
                    new Vector3f(translateX, 0.3f, translateZ),
                    new Quaternionf().rotateX((float) Math.PI),
                    new Vector3f(scaleX, 0.3f, scaleZ),
                    new Quaternionf()
            ));
        }));
    }

    private void spawnMannequin() {
        double feetY = anchorBlock.getBoundingBox().getMaxY() + 0.225;
        Location spawnLoc = new Location(
                anchorBlockLocation.getWorld(),
                anchorBlockLocation.getBlockX() + 0.5,
                feetY,
                anchorBlockLocation.getBlockZ() + 0.5,
                yawFromFacing(facing),
                0f
        );
        spawnLoc.getWorld().getNearbyEntitiesByType(Mannequin.class, spawnLoc, 0.5, 0.5, 0.5)
                .forEach(Mannequin::remove);
        this.mannequin = spawnLoc.getWorld().spawn(spawnLoc, Mannequin.class, m -> {
            m.setImmovable(true);
            m.setPersistent(false);
            m.setInvulnerable(true);
            m.setGravity(false);
            m.setSilent(true);
            m.setCollidable(false);
            m.setAI(false);
            setBaseAttribute(m, Attribute.KNOCKBACK_RESISTANCE, 1.0);
            setBaseAttribute(m, Attribute.SCALE, scale);
            setBaseAttribute(m, Attribute.MAX_HEALTH, 1024.0);
            m.setHealth(1024.0);
        });
    }

    private static void setBaseAttribute(Mannequin m, Attribute attribute, double value) {
        if (m.getAttribute(attribute) != null) {
            m.getAttribute(attribute).setBaseValue(value);
        }
    }

    /** Applies an active clan to this slot: spawns the mannequin if needed, then skins, armors, signs and banners it. */
    public void fill(OfflinePlayer leader, ItemStack[] armor, ItemStack banner, Component[] signLines) {
        if (mannequin == null || !mannequin.isValid()) spawnMannequin();
        if (showBase && baseDisplay.isEmpty()) spawnBaseDisplay();
        setProfile(leader);
        setArmor(armor);
        setSignLines(signLines);
        setBanner(banner);
    }

    /** Empties this slot: removes the mannequin, pedestal and banner and blanks the sign, keeping the anchor/sign loaded. */
    public void clear() {
        if (mannequin != null) {
            mannequin.remove();
            mannequin = null;
        }
        baseDisplay.forEach(BlockDisplay::remove);
        baseDisplay.clear();
        if (sign != null) {
            for (int i = 0; i < 4; i++) sign.line(i, Component.empty());
            sign.update();
        }
        clearBanner();
    }

    private void setProfile(OfflinePlayer player) {
        if (mannequin == null || player == null) return;
        Mannequin target = mannequin;
        PlayerProfile profile = player.getPlayerProfile();
        profile.update().thenAcceptAsync(resolved -> {
            if (target.isValid()) {
                target.setProfile(ResolvableProfile.resolvableProfile(resolved));
            }
        }, task -> plugin.getServer().getScheduler().runTask(plugin, task));
    }

    private void setArmor(ItemStack[] contents) {
        if (mannequin == null || contents == null) return;
        EntityEquipment equipment = mannequin.getEquipment();
        equipment.setArmorContents(contents);
    }

    private void setSignLines(Component[] lines) {
        if (sign == null || lines == null) return;
        for (int i = 0; i < 4; i++) {
            sign.line(i, i < lines.length && lines[i] != null ? lines[i] : Component.empty());
        }
        sign.update();
    }

    /** Reads the trimmed plain text of a bound sign line (the persisted value from a previous run), or "" if no sign. */
    public String readSignLinePlain(int index) {
        if (sign == null || index < 0 || index > 3) return "";
        return PlainTextComponentSerializer.plainText().serialize(sign.line(index)).trim();
    }

    /** Places (or refreshes) the clan's wall banner {@code bannerHeight} blocks above the anchor, facing the display direction. */
    private void setBanner(ItemStack bannerItem) {
        if (bannerItem == null || !(bannerItem.getItemMeta() instanceof BannerMeta bannerMeta)) {
            clearBanner();
            return;
        }
        Block bannerBlock = bannerLocation().getBlock();
        // Don't clobber a block placed in the banner slot after load-time validation; only overwrite air or an existing banner.
        if (!bannerBlock.getType().isAir() && !bannerBlock.getType().name().contains("BANNER")) {
            return;
        }
        String wallBannerType = bannerItem.getType().toString().replace("BANNER", "WALL_BANNER");
        bannerBlock.setType(Material.valueOf(wallBannerType), false);

        if (bannerBlock.getBlockData() instanceof Directional directional) {
            directional.setFacing(facing);
            bannerBlock.setBlockData(directional, false);
        }

        Banner bannerState = (Banner) bannerBlock.getState();
        bannerState.setPatterns(bannerMeta.getPatterns());
        bannerState.update();
    }

    private void clearBanner() {
        Block bannerBlock = bannerLocation().getBlock();
        if (bannerBlock.getType().name().contains("BANNER")) {
            bannerBlock.setType(Material.AIR, false);
        }
    }

    private Location bannerLocation() {
        return anchorBlockLocation.clone().add(0, bannerHeight, 0);
    }

    /** The banner slot must be empty or already hold a banner (a banner from a previous run persists as a real block). */
    private boolean isBannerSpaceClear() {
        Material type = bannerLocation().getBlock().getType();
        return type.isAir() || type.name().contains("BANNER");
    }

    public void remove() {
        if (mannequin != null) {
            mannequin.remove();
            mannequin = null;
        }
        baseDisplay.forEach(BlockDisplay::remove);
        baseDisplay.clear();
        clearBanner();
        // Remove our sign block so it can't linger as an orphan when the row shrinks or an anchor moves.
        // The inactive display keeps its sign so its cursor can be restored from it on the next start.
        if (!keepSign && anchorBlock != null) {
            Block signBlock = anchorBlock.getRelative(facing);
            if (signBlock.getState() instanceof Sign) {
                signBlock.setType(Material.AIR, false);
            }
        }
        sign = null;
        World world = anchorBlockLocation.getWorld();
        for (long[] c : heldChunks) {
            world.removePluginChunkTicket((int) c[0], (int) c[1], plugin);
        }
        heldChunks.clear();
    }

    /** Marks this display's sign to survive {@link #remove()} (used by the inactive display for cursor restore). */
    public void setKeepSign(boolean keepSign) {
        this.keepSign = keepSign;
    }

    public boolean isValid() {
        return valid;
    }

    private static float yawFromFacing(BlockFace face) {
        return switch (face) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> -90f;
            default -> 0f;
        };
    }

    private static String coordString(Location l) {
        return l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ();
    }
}
