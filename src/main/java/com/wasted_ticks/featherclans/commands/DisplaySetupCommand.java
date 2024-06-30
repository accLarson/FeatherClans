package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DisplaySetupCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public DisplaySetupCommand(FeatherClans plugin) {
    this.plugin = plugin;
    this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.displaysetup")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) plugin.getDisplayManager().resetDisplays();

        else if (args.length == 0) plugin.getDisplayManager().addSetUpPlayer(((Player) sender).getPlayer());

        return true;
    }
}
