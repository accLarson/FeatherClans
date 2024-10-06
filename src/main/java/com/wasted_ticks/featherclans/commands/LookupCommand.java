package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LookupCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public LookupCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String arg, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.lookup")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }


        Player originator = (Player) sender;

        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_lookup_no_player", null));
            return true;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

        if (!plugin.getMembershipManager().isOfflinePlayerInClan(offlinePlayer)) {
            originator.sendMessage(messages.get("clan_lookup_not_in_clan", null));
            return true;
        }

        String clan = this.plugin.getMembershipManager().getClanByOfflinePlayer(originator);

        plugin.getLogger().info("Passing command to RosterCommand");

        plugin.getCommandHandler().onCommand(sender, plugin.getCommand("clan"), "clan", new String[]{"roster", clan});
        return false;
    }
}
