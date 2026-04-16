package dev.zerek.featherclans.managers;

import dev.zerek.featherclans.FeatherClans;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class RallyManager {

    private final FeatherClans plugin;
    private final Set<String> recentRallies = new HashSet<>();

    public RallyManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(String clan) {
        return recentRallies.contains(clan.toLowerCase());
    }

    public void markRally(String clan) {
        String key = clan.toLowerCase();
        recentRallies.add(key);
        int cooldown = plugin.getFeatherClansConfig().getClanRallyCooldownSeconds();
        Bukkit.getScheduler().runTaskLater(plugin, () -> recentRallies.remove(key), cooldown * 20L);
    }
}