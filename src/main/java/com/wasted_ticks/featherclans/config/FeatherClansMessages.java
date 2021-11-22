package com.wasted_ticks.featherclans.config;

import com.wasted_ticks.featherclans.FeatherClans;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatherClansMessages {

    private final FeatherClans plugin;
    private final Map<String, TextComponent> messages;
    private FileConfiguration config;

    public FeatherClansMessages(FeatherClans plugin) {
        messages = new HashMap<>();
        this.plugin = plugin;
        this.init();
        this.load();
    }

    private void load() {
        Set<String> keys = config.getKeys(false);
        for (String key: keys) {
            TextComponent value =  (TextComponent) MiniMessage.builder()
                    .removeDefaultTransformations()
                    .transformation(TransformationType.COLOR)
                    .transformation(TransformationType.RESET)
                    .build()
                    .parse(config.getString(key));
            messages.put(key, value);
        }
    }

    private void init() {
        File file = new File(this.plugin.getDataFolder(), "messages.yml");
        if(!file.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Reader stream = new InputStreamReader(this.plugin.getResource("messages.yml"));
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(stream);
        config.options().copyDefaults(true);
        config.setDefaults(defaultConfig);
        try {
            config.save(file);
        } catch (IOException e) {}

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public TextComponent get(String key){
        return messages.get(key);
    }

    public String getAsString(String key) {
        if(messages.containsKey(key)) {
            return LegacyComponentSerializer.legacySection().serialize(messages.get(key));
        }
        else return null;
    }

}