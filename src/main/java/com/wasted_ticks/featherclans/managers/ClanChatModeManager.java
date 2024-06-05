package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ClanChatModeManager {

    private final FeatherClans plugin;
    List<Player> inClanChatMode = new ArrayList<>();

    public ClanChatModeManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        inClanChatMode.add(player);
    }

    public void removePlayer(Player player) {
        inClanChatMode.remove(player);
    }

    public boolean isInClanChatMode(Player player) {
        return inClanChatMode.contains(player);
    }

    public void sendClanChat (CommandSender sender, String[] args) {

        plugin.getCommandHandler().onCommand(sender, plugin.getCommand("clan"), "clan", args);
    }
}
