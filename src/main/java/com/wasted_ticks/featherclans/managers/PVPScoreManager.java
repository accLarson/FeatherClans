package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

public class PVPScoreManager {

    private final FeatherClans plugin;
    private final ClanManager manager;

    Map<OfflinePlayer,Map<OfflinePlayer,Integer>> killMap = new HashMap<>();
    Map<OfflinePlayer,Integer> scoreMap = new HashMap<>();

    public PVPScoreManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.manager = plugin.getClanManager();
        this.init();
    }

    private void init() {
        manager.getAllClanMembers().forEach( offlinePlayer -> {
            killMap.put(offlinePlayer, manager.getClanMemberKillData(offlinePlayer));

            this.updateScore(offlinePlayer);
        });
    }

    private void updateScore(OfflinePlayer offlinePlayer) {
        scoreMap.put(offlinePlayer,0);
        killMap.get(offlinePlayer).forEach((killed, killCount) -> {

            if (killCount > killMap.get(killed).getOrDefault(offlinePlayer,0)) {
                scoreMap.put(offlinePlayer,scoreMap.getOrDefault(offlinePlayer,0) + 1);
            }
        });
    }

    public void addKill(OfflinePlayer killer, OfflinePlayer killed) {
        killMap.compute(killer, (key, value) -> {
            if (value == null) value = new HashMap<>(Map.of(killed, 1));
            else value.put(killed, value.getOrDefault(killed, 1) + 1);

            this.updateScore(killer);
            this.updateScore(killed);

            return value;
        });
    }

    public int getScore(OfflinePlayer offlinePlayer) {
        return scoreMap.getOrDefault(offlinePlayer,0);
    }
}

