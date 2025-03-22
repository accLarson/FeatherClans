package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UpdateDisplayCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public UpdateDisplayCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("feather.clans.updatedisplay")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }
        
        // Trigger the display update
        plugin.getDisplayManager().resetDisplays();
        
        // Send success message
        sender.sendMessage(messages.get("clan_updatedisplay_success", null));
        
        return true;
    }
}
