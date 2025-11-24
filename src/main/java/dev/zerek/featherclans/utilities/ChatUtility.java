package dev.zerek.featherclans.utilities;


import dev.zerek.featherclans.FeatherClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        for (char c : string.toCharArray()) stringWidth += dictionary.get(String.valueOf(c));
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


}
