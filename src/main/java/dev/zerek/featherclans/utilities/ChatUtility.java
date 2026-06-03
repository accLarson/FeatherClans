package dev.zerek.featherclans.utilities;


import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.managers.ChatToggleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtility {

    private final FeatherClans plugin;
    private final Map<String, Integer> dictionary = new HashMap<String, Integer>();

    public ChatUtility(FeatherClans plugin) {
        this.plugin = plugin;
        this.init();
    }

    public void init() {
        File file = new File(Bukkit.getPluginManager().getPlugin("FeatherClans").getDataFolder(), "characters.yml");
        if (!file.exists()) {
            Bukkit.getPluginManager().getPlugin("FeatherClans").saveResource("characters.yml", false);
        }
        YamlConfiguration characters = YamlConfiguration.loadConfiguration(file);
        characters.getKeys(false).forEach(key -> dictionary.put((String) characters.get(key + ".character"), characters.getInt(key + ".width")));
    }


    public int getWidth(Component component) {
        String string = PlainTextComponentSerializer.plainText().serialize(component);
        int stringWidth = 0;
        for (char c : string.toCharArray()) stringWidth += dictionary.getOrDefault(String.valueOf(c), 5);
        return stringWidth;
    }

    public Component addSpacing(Component component, int pixels) {
        return this.addSpacing(component, pixels, false);
    }

    public Component addSpacing(Component component, int pixels, boolean isRightAligned) {
        double difference = pixels - getWidth(component);
        int addonSpaces;
        int addonBoldSpaces = 0;

        // Calculate how many regular and bold spaces to append to the given string to meet the requested length
        if (difference % 4 == 1 && difference >= 4) {
            addonSpaces = (int) (Math.floor(difference / 4) - 1);
            addonBoldSpaces = 1;
        } else if (difference % 4 == 2 && difference >= 8) {
            addonSpaces = (int) (Math.floor(difference / 4) - 2);
            addonBoldSpaces = 2;
        } else if (difference % 4 == 3 && difference >= 12) {
            addonSpaces = (int) (Math.floor(difference / 4) - 3);
            addonBoldSpaces = 3;
        } else {
            addonSpaces = (int) (difference / 4);
        }
        // Append spaces and bold spaces to the end of the given string
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < addonSpaces; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(org.bukkit.ChatColor.BOLD);
        for (int i = 0; i < addonBoldSpaces; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(org.bukkit.ChatColor.RESET);
        TextComponent spaces = Component.text(String.valueOf(stringBuilder));

        if (isRightAligned) return Component.text("").append(spaces).append(component);
        else return Component.text("").append(component).append(spaces);
    }

    /**
     * Renders a chat message body as a component. Each whole-word, case-insensitive occurrence
     * of a candidate member's name who currently has chat hidden is followed by a hoverable
     * marker. The player's message text is inserted literally (never parsed as MiniMessage), so a
     * player's own color or format tags are shown as plain text rather than applied.
     *
     * @param plugin     plugin instance
     * @param message    the raw chat message
     * @param candidates members whose names should be checked (clan, or clan + ally)
     * @return the rendered message body component
     */
    public static Component markHiddenMembers(FeatherClans plugin, String message, Collection<OfflinePlayer> candidates) {
        FeatherClansMessages messages = plugin.getFeatherClansMessages();
        ChatToggleManager toggles = plugin.getChatToggleManager();

        Component body = Component.text(message);

        Map<String, String> hidden = new HashMap<>();
        for (OfflinePlayer candidate : candidates) {
            if (toggles.isChatHidden(candidate.getUniqueId())) {
                String name = candidate.getName();
                if (name != null) {
                    hidden.put(name.toLowerCase(), name);
                }
            }
        }
        if (hidden.isEmpty()) {
            return body;
        }

        String alternation = hidden.keySet().stream().map(Pattern::quote).collect(Collectors.joining("|"));
        Pattern pattern = Pattern.compile("(?i)\\b(?:" + alternation + ")\\b");

        return body.replaceText(config -> config
                .match(pattern)
                .replacement((result, matched) -> {
                    String canonical = hidden.get(result.group().toLowerCase());
                    Component marker = messages.get("clan_hidechat_marker", null)
                            .hoverEvent(HoverEvent.showText(
                                    messages.get("clan_hidechat_hover", Map.of("player", canonical))));
                    return matched.append(marker);
                }));
    }
}