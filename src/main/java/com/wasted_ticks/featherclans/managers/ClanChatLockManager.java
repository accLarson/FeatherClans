package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClanChatLockManager {

    private final Set<UUID> lockedPlayers;
    private final Set<UUID> partnerLockedPlayers;
    private final FeatherClans plugin;

    public ClanChatLockManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.lockedPlayers = new HashSet<>();
        this.partnerLockedPlayers = new HashSet<>();
    }

    public void addPlayer(Player player) {
        lockedPlayers.add(player.getUniqueId());
    }

    public void addPlayerToPartnerChat(Player player) {
        partnerLockedPlayers.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        lockedPlayers.remove(player.getUniqueId());
    }

    public void removePlayerFromPartnerChat(Player player) {
        partnerLockedPlayers.remove(player.getUniqueId());
    }

    public boolean isInClanChatLock(Player player) {
        return lockedPlayers.contains(player.getUniqueId());
    }

    public boolean isInPartnerChatLock(Player player) {
        return partnerLockedPlayers.contains(player.getUniqueId());
    }

    public void removeAllPlayers() {
        lockedPlayers.clear();
        partnerLockedPlayers.clear();
    }
}
