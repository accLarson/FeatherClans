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

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.disband")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!manager.isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan disband")));
            return true;
        }

        String tag = manager.getClanByOfflinePlayer(originator);

        List<OfflinePlayer> members = manager.getOfflinePlayersByClan(tag);

        members.forEach(manager::resignOfflinePlayer);

        boolean successful = manager.deleteClan(tag);

        originator.sendMessage(messages.get("clan_disband_success", Map.of("clan", tag)));

        plugin.getServer()
                .getOnlinePlayers()
                .forEach(p -> p.sendMessage(messages.get("clan_disband_broadcast", Map.of("clan", tag.toLowerCase()))));

        plugin.getActiveManager().removeClan(tag.toLowerCase());

        if(!successful) {
            originator.sendMessage(messages.get("clan_disband_error_generic", null));
        }

        return true;
    }
}
