package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;


    public ChatCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("ChatCommand");
        Player originator = (Player) sender;
        if(plugin.getClanManager().isOfflinePlayerInClan(originator) && args.length < 2) {

            String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

            Clan clan = plugin.getClanManager().getClanByOfflinePlayer(originator);
            List<OfflinePlayer> players = plugin.getClanManager().getOfflinePlayersByClan(clan);
            for (OfflinePlayer player: players) {
                if(player.isOnline()) {
                    player.getPlayer().sendMessage(message);
                }
            }

        } else {
            sender.sendMessage(messages.get("clan_command_error"));
        }
        return false;
    }
}
