package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

public class PVPScoreManager {

    private final FeatherClans plugin;
    private final ClanManager clanManager;

    Map<OfflinePlayer,Map<OfflinePlayer,Integer>> killMap = new HashMap<>();
    Map<OfflinePlayer,Integer> scoreMap = new HashMap<>();

    public PVPScoreManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.init();
        killMap.forEach((k,v) -> System.out.println(k + " " + v));
    }

    private void init() {
        clanManager.getAllClanMembers().forEach(offlinePlayer -> killMap.put(offlinePlayer, clanManager.getClanMemberKillData(offlinePlayer)));
        killMap.forEach((killer, kills) -> this.updateScore(killer));
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
            else value.put(killed, value.getOrDefault(killed, 0) + 1);

            this.updateScore(killer);
            this.updateScore(killed);

            return value;
        });
    }

    public int getScore(OfflinePlayer offlinePlayer) {
        return scoreMap.getOrDefault(offlinePlayer,0);
    }
    public int getScore(String tag){
        return clanManager.getOfflinePlayersByClan(tag).stream().mapToInt(member -> scoreMap.getOrDefault(member, 0)).sum();
    }

    public Map<OfflinePlayer, Integer> getKills(OfflinePlayer offlinePlayer) {
        return killMap.getOrDefault(offlinePlayer, new HashMap<>());
    }
}

