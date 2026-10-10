package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Advances the rotating display to the next clan (alphabetical, wrapping). Op-only.
 */
public class CycleRotatingCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public CycleRotatingCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.clans.cyclerotating")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getDisplayManager().isRotatingDisplayEnabled()) {
            sender.sendMessage(messages.get("clan_cycle_disabled", null));
            return true;
        }

        String tag = plugin.getDisplayManager().cycleRotatingDisplay();
        if (tag == null) {
            sender.sendMessage(messages.get("clan_cycle_empty", null));
        } else {
            sender.sendMessage(messages.get("clan_cycle_success", Map.of("clan", tag)));
        }
        return true;
    }
}
