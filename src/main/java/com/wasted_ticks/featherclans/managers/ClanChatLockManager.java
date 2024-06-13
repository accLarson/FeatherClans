package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ClanChatLockManager {

    private final FeatherClans plugin;
    List<Player> inChatLock = new ArrayList<>();

    public ClanChatLockManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        inChatLock.add(player);
    }

    public void removePlayer(Player player) {
        inChatLock.remove(player);
    }

    public boolean isInClanChatLock(Player player) {
        return inChatLock.contains(player);
    }

    public void sendClanChat (CommandSender sender, String[] args) {

        plugin.getCommandHandler().onCommand(sender, plugin.getCommand("clan"), "clan", args);
    }
}
