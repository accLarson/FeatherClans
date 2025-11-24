package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ReloadCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.clans.reload")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        plugin.reload();
        sender.sendMessage(messages.get("clan_reload_success", null));
        return true;
    }
}
