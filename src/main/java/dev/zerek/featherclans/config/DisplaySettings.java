package dev.zerek.featherclans.config;

import org.bukkit.Location;

import java.util.List;

/**
 * Resolved configuration for a single clan display section (active / latest / rotate-display).
 *
 * @param enabled      whether this display is built at all
 * @param anchor       the first anchor block; the active row extends to the viewer's right from here
 * @param facing       direction the mannequins / signs / banners face (toward viewers)
 * @param count        number of slots (1 for the single displays)
 * @param spacing      empty blocks between slots in a row (0 = adjacent)
 * @param scale        mannequin size (shared across displays)
 * @param base         whether to show the hopper pedestal (shared across displays)
 * @param bannerHeight blocks above the anchor to place the wall banner (shared across displays)
 * @param signType     wall sign wood type (shared across displays)
 * @param align        how filled slots sit in the row when fewer clans are active than the slot
 *                     count: 'center' (offset left when uneven) or 'left' (anchored). Only the active
 *                     row has multiple slots, so it's a no-op for the single displays.
 * @param signLines    the four MiniMessage sign templates (placeholders: &lt;tag&gt;, &lt;leader&gt;, &lt;active&gt;)
 */
public record DisplaySettings(
        boolean enabled,
        Location anchor,
        String facing,
        int count,
        int spacing,
        double scale,
        boolean base,
        int bannerHeight,
        String signType,
        String align,
        List<String> signLines) {
}
