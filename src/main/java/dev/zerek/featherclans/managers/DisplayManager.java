package dev.zerek.featherclans.managers;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.DisplaySettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Coordinates the clan displays. Three independent groups are built from config, each from
 * {@link ClanDisplay} units:
 * <ul>
 *   <li><b>active</b> — a row of the top active clans (ordered), filling to the viewer's right.</li>
 *   <li><b>latest</b> — a single mannequin showing the newest clan created.</li>
 *   <li><b>inactive</b> — a single mannequin showing one inactive clan, advanced by {@code /clan cycle}.</li>
 * </ul>
 * All groups are kept consistent through {@link #resetDisplays()}, which the existing command and
 * active-status call sites already invoke.
 */
public class DisplayManager {

    private final FeatherClans plugin;

    private final List<ClanDisplay> activeSlots = new ArrayList<>();
    private DisplaySettings activeSettings;

    private ClanDisplay latestDisplay;
    private DisplaySettings latestSettings;

    private ClanDisplay inactiveDisplay;
    private DisplaySettings inactiveSettings;
    private String inactiveCursor;

    private boolean loaded;

    public DisplayManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    /**
     * Builds every enabled display group from config and, once all slots have loaded, fills them
     * from the current clan state. No-op slots are skipped for disabled sections.
     */
    public CompletableFuture<Void> load() {
        var config = plugin.getFeatherClansConfig();
        this.activeSettings = config.getActiveDisplay();
        this.latestSettings = config.getLatestDisplay();
        this.inactiveSettings = config.getInactiveDisplay();

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // Active row.
        activeSlots.clear();
        if (activeSettings.enabled()) {
            BlockFace facing = parseFacing(activeSettings.facing());
            Material signMaterial = resolveSignMaterial(activeSettings.signType());
            BlockFace right = rightOf(facing);
            int step = Math.max(0, activeSettings.spacing()) + 1;
            int count = Math.max(0, activeSettings.count());
            for (int i = 0; i < count; i++) {
                final int idx = i;
                int dist = i * step;
                Location loc = activeSettings.anchor().clone().add(right.getModX() * dist, 0, right.getModZ() * dist);
                ClanDisplay slot = new ClanDisplay(plugin, loc, facing, activeSettings.scale(),
                        activeSettings.base(), activeSettings.bannerHeight(), signMaterial);
                activeSlots.add(slot);
                futures.add(slot.load().exceptionally(t -> {
                    plugin.getLogger().severe("Active display slot " + idx + " failed to load: " + t.getMessage());
                    return false;
                }));
            }
        }

        // Single displays.
        this.latestDisplay = buildSingle(latestSettings, futures, "Latest");
        this.inactiveDisplay = buildSingle(inactiveSettings, futures, "Inactive");
        // The inactive sign must persist across restarts so its cursor can be restored from it.
        if (inactiveDisplay != null) inactiveDisplay.setKeepSign(true);

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        approveAndPlace();
                        restoreInactiveCursor();
                        loaded = true;
                        resetDisplays();
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to initialize clan displays: " + e.getMessage());
                    }
                }));
    }

    /**
     * On startup, recovers the inactive display's cursor from its persisted sign (the inactive sign is
     * a real block that survives restarts). If the tag read back still exists and is still inactive,
     * the cursor resumes there; otherwise it's left unset and {@link #refreshInactive} falls back to
     * the first inactive clan alphabetically.
     */
    private void restoreInactiveCursor() {
        if (inactiveDisplay == null) return;
        int tagLine = tagLineIndex(inactiveSettings.signLines());
        if (tagLine < 0) return;
        String shown = inactiveDisplay.readSignLinePlain(tagLine);
        if (shown.isEmpty()) return;
        String match = plugin.getClanManager().getClans().stream()
                .filter(tag -> tag.equalsIgnoreCase(shown))
                .findFirst()
                .orElse(null);
        if (match != null && !plugin.getActiveManager().isActive(match)) {
            inactiveCursor = match;
        }
    }

    /** Index of the sign template line carrying the {@code <tag>} placeholder, or -1 if none. */
    private static int tagLineIndex(List<String> templates) {
        for (int i = 0; i < templates.size(); i++) {
            if (templates.get(i) != null && templates.get(i).contains("<tag>")) return i;
        }
        return -1;
    }

    /**
     * Validates each display group as a whole and places only fully-valid groups. The active row is
     * all-or-nothing: if any one of its slots failed validation (missing/non-solid anchor, blocked
     * air column, occupied banner/sign), the entire row is discarded and nothing is placed. The
     * latest and inactive single displays are each judged independently. Validation places nothing,
     * so a discarded group leaves no blocks behind — only its chunk tickets are released.
     */
    private void approveAndPlace() {
        // Active row: place every slot only if all of them validated; otherwise discard the whole row.
        if (!activeSlots.isEmpty()) {
            long failed = activeSlots.stream().filter(slot -> !slot.isValid()).count();
            if (failed == 0) {
                activeSlots.forEach(ClanDisplay::place);
            } else {
                plugin.getLogger().warning("Active display not shown: " + failed + " of " + activeSlots.size()
                        + " slot(s) are invalid (see warnings above). The whole active row stays hidden until every spot is valid.");
                activeSlots.forEach(ClanDisplay::remove);
                activeSlots.clear();
            }
        }
        // Single displays are independent of the active row and of each other.
        latestDisplay = approveSingle(latestDisplay, "Latest");
        inactiveDisplay = approveSingle(inactiveDisplay, "Inactive");
    }

    private ClanDisplay approveSingle(ClanDisplay display, String name) {
        if (display == null) return null;
        if (display.isValid()) {
            display.place();
            return display;
        }
        display.remove();
        plugin.getLogger().warning(name + " display not shown — its location is invalid (see warnings above).");
        return null;
    }

    private ClanDisplay buildSingle(DisplaySettings settings, List<CompletableFuture<Boolean>> futures, String label) {
        if (!settings.enabled()) return null;
        BlockFace facing = parseFacing(settings.facing());
        Material signMaterial = resolveSignMaterial(settings.signType());
        ClanDisplay display = new ClanDisplay(plugin, settings.anchor().clone(), facing,
                settings.scale(), settings.base(), settings.bannerHeight(), signMaterial);
        futures.add(display.load().exceptionally(t -> {
            plugin.getLogger().severe(label + " display failed to load: " + t.getMessage());
            return false;
        }));
        return display;
    }

    /**
     * Re-fills all three display groups from the current clan state. No-op until {@link #load()} has
     * finished binding the slots; every existing caller (commands + active-status changes) routes
     * through here, so latest and inactive stay consistent alongside the active row.
     */
    public void resetDisplays() {
        if (!loaded) return;

        List<String> activeClans = plugin.getActiveManager().getActiveClansOrdered();
        for (int i = 0; i < activeSlots.size(); i++) {
            ClanDisplay slot = activeSlots.get(i);
            try {
                if (i >= activeClans.size()) {
                    slot.clear();
                } else {
                    fillSlot(slot, activeClans.get(i), activeSettings.signLines());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to update active display slot " + i + ": " + e.getMessage());
            }
        }

        try {
            updateLatest();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update latest-clan display: " + e.getMessage());
        }
        try {
            refreshInactive(false);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update inactive-clan display: " + e.getMessage());
        }
    }

    /** Refreshes the latest-clan display with the newest clan created (or clears it if none). */
    public void updateLatest() {
        if (latestDisplay == null) return;
        String tag = plugin.getClanManager().getNewestClan();
        if (tag == null) {
            latestDisplay.clear();
            return;
        }
        fillSlot(latestDisplay, tag, latestSettings.signLines());
    }

    /**
     * Re-validates the inactive display against the current inactive set. If {@code reset} (or the
     * cursor is no longer inactive), it snaps back to the first inactive clan; clears if none remain.
     */
    private void refreshInactive(boolean reset) {
        if (inactiveDisplay == null) return;
        List<String> inactive = orderedInactiveClans();
        if (inactive.isEmpty()) {
            inactiveDisplay.clear();
            inactiveCursor = null;
            return;
        }
        if (reset || inactiveCursor == null || !inactive.contains(inactiveCursor)) {
            inactiveCursor = inactive.get(0);
        }
        fillSlot(inactiveDisplay, inactiveCursor, inactiveSettings.signLines());
    }

    /**
     * Advances the inactive display to the next inactive clan (alphabetical, wrapping).
     *
     * @return the now-shown clan tag, or null if the display is disabled/not loaded or there are no
     *         inactive clans.
     */
    public String cycleInactive() {
        if (!loaded || inactiveDisplay == null) return null;
        List<String> inactive = orderedInactiveClans();
        if (inactive.isEmpty()) {
            inactiveDisplay.clear();
            inactiveCursor = null;
            return null;
        }
        int idx = (inactiveCursor == null) ? -1 : inactive.indexOf(inactiveCursor);
        int next = (idx < 0) ? 0 : (idx + 1) % inactive.size();
        inactiveCursor = inactive.get(next);
        fillSlot(inactiveDisplay, inactiveCursor, inactiveSettings.signLines());
        return inactiveCursor;
    }

    /** Whether the inactive display is configured/enabled (regardless of whether any clan is inactive). */
    public boolean isInactiveDisplayEnabled() {
        return inactiveDisplay != null;
    }

    public void removeAll() {
        activeSlots.forEach(ClanDisplay::remove);
        activeSlots.clear();
        if (latestDisplay != null) {
            latestDisplay.remove();
            latestDisplay = null;
        }
        if (inactiveDisplay != null) {
            inactiveDisplay.remove();
            inactiveDisplay = null;
        }
        inactiveCursor = null;
        loaded = false;
    }

    /** All clans that are not currently active, ordered alphabetically by tag. */
    private List<String> orderedInactiveClans() {
        return plugin.getClanManager().getClans().stream()
                .filter(tag -> !plugin.getActiveManager().isActive(tag))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Skins/armors/signs/banners a slot for the given clan, or clears it if the clan has no leader.
     *
     * @return true if the slot was filled, false if it was cleared.
     */
    private boolean fillSlot(ClanDisplay slot, String clanTag, List<String> signTemplates) {
        UUID leaderUuid = plugin.getClanManager().getLeader(clanTag);
        if (leaderUuid == null) {
            slot.clear();
            return false;
        }
        OfflinePlayer leader = Bukkit.getOfflinePlayer(leaderUuid);

        // Every piece is optional; a null slot renders no armor there (the skin shows through).
        // EntityEquipment armor order: [boots, leggings, chestplate, helmet].
        ItemStack[] armor = new ItemStack[]{
                plugin.getClanManager().getBoots(clanTag),
                plugin.getClanManager().getLeggings(clanTag),
                plugin.getClanManager().getChestplate(clanTag),
                plugin.getClanManager().getHelmet(clanTag)
        };

        ItemStack banner = plugin.getClanManager().getBanner(clanTag);

        slot.fill(leader, armor, banner, renderSign(clanTag, leader, signTemplates));
        return true;
    }

    private Component[] renderSign(String clanTag, OfflinePlayer leader, List<String> signTemplates) {
        String coloredTag = plugin.getClanManager().getColorTag(clanTag);
        String tagText = (coloredTag != null) ? coloredTag : clanTag;
        String leaderName = (leader.getName() != null) ? leader.getName() : "Unknown";
        int activeCount = plugin.getActiveManager().getActiveMemberCount(clanTag);

        MiniMessage mm = MiniMessage.miniMessage();
        Component tagComponent = mm.deserialize(tagText);

        Component[] out = new Component[4];
        for (int i = 0; i < 4; i++) {
            String template = (i < signTemplates.size() && signTemplates.get(i) != null) ? signTemplates.get(i) : "";
            out[i] = mm.deserialize(template,
                    Placeholder.component("tag", tagComponent),
                    Placeholder.unparsed("leader", leaderName),
                    Placeholder.unparsed("active", String.valueOf(activeCount)));
        }
        return out;
    }

    private Material resolveSignMaterial(String configSignType) {
        try {
            return Material.valueOf(configSignType.toUpperCase() + "_WALL_SIGN");
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sign_type in config: " + configSignType + ". Defaulting to OAK_WALL_SIGN");
            return Material.OAK_WALL_SIGN;
        }
    }

    private BlockFace parseFacing(String facing) {
        try {
            return BlockFace.valueOf(facing.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            plugin.getLogger().warning("Invalid display facing in config: " + facing + ". Defaulting to NORTH");
            return BlockFace.NORTH;
        }
    }

    /** The viewer's right, given the direction the displays face (facing rotated 90° CCW). */
    private static BlockFace rightOf(BlockFace facing) {
        return switch (facing) {
            case SOUTH -> BlockFace.EAST;
            case NORTH -> BlockFace.WEST;
            case EAST -> BlockFace.NORTH;
            case WEST -> BlockFace.SOUTH;
            default -> BlockFace.EAST;
        };
    }
}
