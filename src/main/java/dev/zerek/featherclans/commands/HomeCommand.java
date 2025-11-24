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

public class HomeCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public HomeCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.home")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("feather.clans.home.others") && args.length >= 2) {

            String tag = args[1].toLowerCase();

            if (plugin.getClanManager().getClans().contains(tag)) {

                if (plugin.getClanManager().hasClanHome(tag)) {

                    Location clanHomeLocation = plugin.getClanManager().getClanHome(tag);
                    player.teleport(clanHomeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    player.sendMessage(messages.get("clan_home_teleport_success", Map.of(
                            "clan", tag
                    )));
                }
                else player.sendMessage(messages.get("clan_home_teleport_error_no_home",null));
            }
            else player.sendMessage(messages.get("clan_home_teleport_error_admin_no_clan",null));
        }

        else if (plugin.getClanManager().isOfflinePlayerInClan(player)) {

            if (args.length != 1) {
                sender.sendMessage(messages.get("clan_home_teleport_error_invalid_arg_length",null));
                return true;
            }

            String tag = plugin.getClanManager().getClanByOfflinePlayer(player);

            if (plugin.getClanManager().hasClanHome(tag)) {

                int delay = this.plugin.getFeatherClansConfig().getClanTeleportDelaySeconds();

                player.sendMessage(messages.get("clan_home_teleport_initiate", Map.of(
                        "delay", String.valueOf(delay)
                )));

                Location clanHomeLocation = plugin.getClanManager().getClanHome(tag);

                TeleportTimerUtility timer = new TeleportTimerUtility(this.plugin, delay, null, () -> {
                    player.sendMessage(messages.get("clan_home_teleport_success", Map.of(
                            "clan", tag
                    )));
                    player.teleport(clanHomeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }, (instance) -> {
                    if (!instance.getStartLocation().getBlock().equals(player.getLocation().getBlock())) {
                        player.sendMessage(messages.get("clan_home_teleport_failure", null));
                        instance.cancel();
                    }
                }, player.getLocation());
                timer.start();
            }
            else player.sendMessage(messages.get("clan_home_teleport_error_no_home", null));

        }
        else player.sendMessage(messages.get("clan_home_teleport_error_no_clan", null));

        return true;
    }
}
