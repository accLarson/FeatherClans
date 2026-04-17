package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.utilities.TeleportTimerUtility;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AllyHomeCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public AllyHomeCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.allyhome")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("feather.clans.allyhome.others") && args.length >= 2) {

            String tag = args[1].toLowerCase();

            if (!plugin.getClanManager().getClans().contains(tag)) {
                player.sendMessage(messages.get("clan_home_teleport_error_admin_no_clan", null));
                return true;
            }

            if (!plugin.getClanManager().hasAlly(tag)) {
                player.sendMessage(messages.get("clan_ally_error_no_alliance", null));
                return true;
            }

            Location allyHomeLocation = plugin.getClanManager().getAllyHome(tag);
            if (allyHomeLocation == null) {
                player.sendMessage(messages.get("clan_allyhome_teleport_error_no_home", null));
                return true;
            }

            String allyTag = plugin.getClanManager().getAlly(tag);
            player.teleport(allyHomeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.sendMessage(messages.get("clan_allyhome_teleport_success", Map.of(
                    "ally", allyTag
            )));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_home_teleport_error_no_clan", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(player);

        if (!plugin.getClanManager().hasAlly(tag)) {
            player.sendMessage(messages.get("clan_ally_error_no_alliance", null));
            return true;
        }

        Location allyHomeLocation = plugin.getClanManager().getAllyHome(tag);
        if (allyHomeLocation == null) {
            player.sendMessage(messages.get("clan_allyhome_teleport_error_no_home", null));
            return true;
        }

        int delay = plugin.getFeatherClansConfig().getClanTeleportDelaySeconds();

        player.sendMessage(messages.get("clan_home_teleport_initiate", Map.of(
                "delay", String.valueOf(delay)
        )));

        String allyTag = plugin.getClanManager().getAlly(tag);

        TeleportTimerUtility timer = new TeleportTimerUtility(plugin, delay, null, () -> {
            player.sendMessage(messages.get("clan_allyhome_teleport_success", Map.of(
                    "ally", allyTag
            )));
            player.teleport(allyHomeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, (instance) -> {
            if (!instance.getStartLocation().getBlock().equals(player.getLocation().getBlock())) {
                player.sendMessage(messages.get("clan_home_teleport_failure", null));
                instance.cancel();
            }
        }, player.getLocation());
        timer.start();

        return true;
    }
}