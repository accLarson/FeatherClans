package com.wasted_ticks.featherclans.managers;

import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

public class PVPScoreManager {

    Map<OfflinePlayer,Map<OfflinePlayer,Integer>> killMap = new HashMap<>();

    public void addKill(OfflinePlayer killer, OfflinePlayer killed) {
        if (killMap.containsKey(killer)) {
            Integer killCount = killMap.get(killer).getOrDefault(killed, 0);
            killMap.get(killer).put(killed, killCount + 1);
        } else {
            killMap.put(killer, new HashMap<>(Map.of(killed, 1)));
        }
    }
}
