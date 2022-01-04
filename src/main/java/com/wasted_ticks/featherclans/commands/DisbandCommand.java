package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DisbandCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final ClanManager manager;
    private final FeatherClansMessages messages;

    public DisbandCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.manager = plugin.getClanManager();
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;
            boolean isLeader = manager.isOfflinePlayerLeader(player);

            if(isLeader) {
                String tag = manager.getClanByOfflinePlayer(player);
                List<OfflinePlayer> members = manager.getOfflinePlayersByClan(tag);
                for (OfflinePlayer member: members) {
                    manager.resignOfflinePlayer(member);
                }
                manager.deleteClan(tag);
                player.sendMessage(messages.get("clan_disband_success", Map.of(
                        "clan", tag
                )));
                return true;
            } else {
                player.sendMessage(messages.get("clan_error_leader", null));
                return false;
            }
        }
        return false;
    }
}
