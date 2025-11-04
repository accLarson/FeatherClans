package com.wasted_ticks.featherclans.managers;

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

    public boolean isPlayerInList(Player player) {
        return allowingFriendlyFire.contains(player);
    }

    public boolean isAllowingFriendlyFire(Player player) {
        if (player.hasPermission("feather.clans.forcefriendlyfire")) return true;
        return allowingFriendlyFire.contains(player);
    }
}
