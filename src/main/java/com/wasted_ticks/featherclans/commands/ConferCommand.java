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

import java.util.Map;

public class ConferCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ConferCommand(FeatherClans plugin) {
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

        if (!originator.hasPermission("feather.clans.confer")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_confer_no_player", null));
            return true;
        }

        OfflinePlayer potentialLeader = Bukkit.getOfflinePlayer(args[1]);

        if (plugin.getClanManager().isOfflinePlayerLeader(potentialLeader)) {
            originator.sendMessage(messages.get("clan_confer_error_leader", null));
            return true;
        }

        String clan = this.plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (this.plugin.getClanManager().isOfflinePlayerInSpecificClan(potentialLeader, clan)) {
            originator.sendMessage(messages.get("clan_confer_not_in_clan", null));
            return true;
        }

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan confer " + potentialLeader.getName())));
            return true;
        }

        boolean successful = this.plugin.getClanManager().setClanLeader(clan, potentialLeader);

        if (successful) {
            this.plugin.getClanManager().setClanOfficerStatus(potentialLeader, false);
            this.plugin.getClanManager().setClanOfficerStatus(originator, true);

            originator.sendMessage(messages.get("clan_confer_success_originator", Map.of(
                    "player", potentialLeader.getName()
            )));
            if (potentialLeader.isOnline()) {
                ((Player)potentialLeader).sendMessage(messages.get("clan_confer_success_player", Map.of(
                        "player", originator.getName(),
                        "clan", clan
                )));
            }
            this.plugin.getDisplayManager().resetDisplays();
        } else {
            originator.sendMessage(messages.get("clan_confer_error_generic", null));
        }
        return true;

    }
}
