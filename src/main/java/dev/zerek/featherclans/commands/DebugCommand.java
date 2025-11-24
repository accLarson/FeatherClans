package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DebugCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public DebugCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.clans.debug")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }
        
        switch (args[1]) {
            case "updatedisplay":
                plugin.getDisplayManager().resetDisplays();
                sender.sendMessage(messages.get("clan_updatedisplay_success", null));
                break;
            case "getactive":
                plugin.getActiveManager().getActiveClansOrdered().forEach(clanTag -> {
                    // Get active count and all active members for this clan
                    int activeCount = plugin.getActiveManager().getActiveMemberCount(clanTag);
                    List<UUID> activeMembers = plugin.getActiveManager().getActiveMembersInClan(clanTag);
                    
                    // Display clan tag and active count on first line
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>" + clanTag + " <dark_gray>(" + activeCount + " active)"));
                    
                    // Display all active members on a single line
                    String playerList = activeMembers.stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining(", "));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<white>" + playerList));
                });
                break;
            default:
                sender.sendMessage("Unknown debug command: " + args[1]);
        }

        return true;
    }
}
