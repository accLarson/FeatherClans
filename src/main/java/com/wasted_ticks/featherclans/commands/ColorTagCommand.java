package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.ColorTagUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ColorTagCommand implements CommandExecutor {

    private final FeatherClans plugin;

    private final ColorTagUtil colorTagUtil;
    private final FeatherClansMessages messages;


    public ColorTagCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.colorTagUtil = plugin.getColorTagUtil();
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!commandSender.hasPermission("feather.clans.colortag")) {
            commandSender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }


        Player originator = (Player) commandSender;

        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (strings.length != 2) {
            originator.sendMessage(messages.get("clan_colortag_error_invalid_arg_length", null));
            return true;
        }

        String potentialTag = strings[1];
        String originalTag = this.plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (!plugin.getColorTagUtil().isValid(potentialTag, originalTag)) {
            originator.sendMessage(messages.get("clan_colortag_invalid_tag", null));
            return true;
        }

        return false;
    }
}
