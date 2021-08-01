package com.wasted_ticks.featherclans.commands.completers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClanTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList(
            "home",
            "sethome",
            "create",
            "invite",
            "kick",
            "disband",
            "confer",
            "chat",
            "accept",
            "decline",
            "help",
            "list",
            "roster"
    );
    private final FeatherClans plugin;


    public ClanTabCompleter(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        }
        return completions;
    }
}
