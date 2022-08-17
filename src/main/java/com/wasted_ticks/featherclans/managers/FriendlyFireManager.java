package com.wasted_ticks.featherclans.managers;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FriendlyFireManager {

    List<Player> allowingFriendlyFire = new ArrayList<>();

    public void addPlayer(Player player) {
        allowingFriendlyFire.add(player);
    }

    public void removePlayer(Player player) {
        allowingFriendlyFire.remove(player);
    }

    public boolean isAllowingFriendlyFire(Player player) {
        return allowingFriendlyFire.contains(player);
    }
}
