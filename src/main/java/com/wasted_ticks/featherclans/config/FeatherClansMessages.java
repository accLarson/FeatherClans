package com.wasted_ticks.featherclans.config;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.util.ChatUtil;
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
    private final Map<String, String> messages;
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
            String value = config.getString(key);
            messages.put(key, ChatUtil.translateHexColorCodes(value));
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

    public String get(String key){
        return messages.get(key);
    }
}