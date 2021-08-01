package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.util.TeleportTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class HomeCommand implements CommandExecutor {

    private final FeatherClans plugin;

    public HomeCommand(FeatherClans plugin) {
        this.plugin  = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(plugin.getClanManager().isPlayerInClan(player)) {

                ClanMember member = plugin.getClanManager().getClanMemberByPlayer(player);
                Clan clan = plugin.getClanManager().getClanByClanMember(member);

                if(plugin.getClanManager().hasClanHome(clan)) {
                    player.sendMessage("Teleporting to clan home.");

                    Location clanHomeLocation = plugin.getClanManager().getClanHome(clan);

                    int delay = this.plugin.getFeatherConfig().getTeleportDelaySeconds();

                    TeleportTimer timer = new TeleportTimer(this.plugin, delay, null, () -> {
                        player.sendMessage("Teleporting now.");
                        player.teleport(clanHomeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }, (instance) -> {
                        if(instance.getStartLocation().getBlock().equals(player.getLocation().getBlock())) {
                            player.sendMessage(instance.getRemainingSeconds() + "...");
                        } else {
                            player.sendMessage("Movement detected, cancelling teleport.");
                            instance.cancel();
                        }
                    }, player.getLocation());
                    timer.start();

                    return true;
                } else {
                    player.sendMessage("Error: Your clan currently doesn't have a set home location.");
                    return false;
                }
            } else {
                player.sendMessage("Error: You are not currently a member of a clan.");
                return false;
            }

        } else return false;
    }
}
