package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.util.TeleportTimerUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class HomeCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public HomeCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (plugin.getClanManager().isOfflinePlayerLeader(player)) {

                String tag = plugin.getClanManager().getClanByOfflinePlayer(player);

                if (plugin.getClanManager().hasClanHome(tag)) {

                    player.sendMessage(messages.get("clan_home_teleport_initiate", null));

                    Location clanHomeLocation = plugin.getClanManager().getClanHome(tag);
                    int delay = this.plugin.getFeatherClansConfig().getClanTeleportDelaySeconds();
                    TeleportTimerUtil timer = new TeleportTimerUtil(this.plugin, delay, null, () -> {
                        player.sendMessage(messages.get("clan_home_teleport_success", null));
                        player.teleport(clanHomeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }, (instance) -> {
                        if (!instance.getStartLocation().getBlock().equals(player.getLocation().getBlock())) {
                            player.sendMessage(messages.get("clan_home_teleport_failure", null));
                            instance.cancel();
                        }
                    }, player.getLocation());
                    timer.start();

                    return true;
                } else {
                    player.sendMessage(messages.get("clan_home_teleport_error_no_home", null));
                    return false;
                }
            } else {
                player.sendMessage(messages.get("clan_home_teleport_error_no_clan", null));
                return false;
            }
        } else return false;
    }
}
