package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
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
        this.plugin = plugin;
        this.manager = plugin.getClanManager();
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.disband")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        boolean isLeader = manager.isOfflinePlayerLeader(player);
        boolean successful = false;
        if (isLeader) {
            if (!args[0].equalsIgnoreCase("confirm")){
                player.sendMessage(messages.get("clan_confirm_notice", Map.of(
                        "label", label,
                        "args", String.join(" ", args)
                )));
                return true;
            }

                String tag = manager.getClanByOfflinePlayer(player);
            List<OfflinePlayer> members = manager.getOfflinePlayersByClan(tag);
            for (OfflinePlayer member : members) {
                manager.resignOfflinePlayer(member);
            }
            successful = manager.deleteClan(tag);
            player.sendMessage(messages.get("clan_disband_success", Map.of(
                    "clan", tag
            )));
            plugin.getServer()
                    .getOnlinePlayers()
                    .stream()
                    .forEach(p -> p.sendMessage(messages.get("clan_disband_broadcast", Map.of(
                            "clan", tag.toLowerCase()
                    ))));
        } else {
            player.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if(!successful) {
            player.sendMessage(messages.get("clan_disband_error_generic", null));
        }

        return true;
    }
}
