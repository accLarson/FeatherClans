package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ResignCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ResignCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.resign")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerInClan(originator)) {
            originator.sendMessage(messages.get("clan_resign_error_no_clan", null));
            return true;
        }

        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(originator);

        if (leader) {
            originator.sendMessage(messages.get("clan_resign_error_leader", null));
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan resign")));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);

        boolean successful = plugin.getClanManager().resignOfflinePlayer(originator);

        if (!successful) {
            originator.sendMessage(messages.get("clan_resign_error_generic", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_resign_success", null));

        plugin.getActiveManager().updateActiveStatus(originator, tag);

        return true;

    }
}
