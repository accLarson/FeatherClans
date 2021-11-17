package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.util.ChatUtil;
import org.bukkit.ChatColor;
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
        Player originator = (Player) sender;
        if(args.length < 2) {
            originator.sendMessage("This command requires a message argument.");
            return false;
        }

        if(!plugin.getClanManager().isOfflinePlayerInClan(originator)) {
            originator.sendMessage("You must be a member of a clan to use this command.");
            return false;
        }

        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        Clan clan = plugin.getClanManager().getClanByOfflinePlayer(originator);
        String tag = clan.getString("tag");

        StringBuilder builder = new StringBuilder();
        builder.append(ChatUtil.translateHexColorCodes(messages.get("clan_color") + "[" + tag + "]: "));
        builder.append(ChatColor.RESET);
        builder.append(message);

        List<OfflinePlayer> players = plugin.getClanManager().getOfflinePlayersByClan(clan);
        for (OfflinePlayer player: players) {
            if(player.isOnline()) {
                player.getPlayer().sendMessage(builder.toString());
            }
        }

        return true;
    }
}
