package dev.zerek.featherclans.config;

import dev.zerek.featherclans.FeatherClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatherClansMessages {

    private final FeatherClans plugin;
    private final Map<String, String> messages;
    private FileConfiguration config;
    private String themePrimary;

    public FeatherClansMessages(FeatherClans plugin) {
        messages = new HashMap<>();
        this.plugin = plugin;
        this.init();
        this.load();
    }

    private void load() {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            if (key.equals("clan_theme_primary")) {
                this.themePrimary = config.getString(key);
                continue;
            }
            messages.put(key, config.getString(key));
        }
    }

    private void init() {
        File file = new File(this.plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Reader stream = new InputStreamReader(this.plugin.getResource("messages.yml"));
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(stream);
        config.options().copyDefaults(true);
        config.setDefaults(defaultConfig);
        try {
            config.save(file);
        } catch (IOException e) {
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    private MiniMessage parser() {
        return MiniMessage.builder().tags(
                TagResolver.builder()
                        .resolver(StandardTags.color())
                        .resolver(StandardTags.decorations())
                        .resolver(StandardTags.reset())
                        .resolver(StandardTags.newline())
                        .build()
        ).build();
    }

    public TextComponent get(String key, Map<String, String> placeholders) {
        return get(key, placeholders, new TagResolver[0]);
    }

    public TextComponent get(String key, Map<String, String> placeholders, TagResolver... extraResolvers) {
        if (!messages.containsKey(key)) {
            return Component.text("");
        }

        List<TagResolver> rs = new ArrayList<>();
        if (placeholders != null) {
            placeholders.forEach((k, v) -> rs.add(Placeholder.parsed(k, v)));
        }
        if (extraResolvers != null) {
            Collections.addAll(rs, extraResolvers);
        }

        return (TextComponent) parser().deserialize(messages.get(key), TagResolver.resolver(rs));
    }

    public String getThemePrimary() {
        return this.themePrimary;
    }


}