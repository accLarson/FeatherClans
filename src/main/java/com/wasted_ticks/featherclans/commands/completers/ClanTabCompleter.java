package com.wasted_ticks.featherclans.commands.completers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ClanTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList(
        "home",
        "sethome",
        "create",
        "invite",   // needs further autocomplete
        "kick",     // needs further autocomplete
        "disband",
        "confer",   // needs further autocomplete
        "chat",
        "accept",
        "decline",
        "help",
        "list",     // needs further autocomplete
        "roster"    // needs further autocomplete
    );
    private final FeatherClans plugin;


    public ClanTabCompleter(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        switch(args.length) {

            case 1:
                StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
                break;
            case 2:
                if("invite".equals(args[0])) {
                    completions = plugin.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
                } else if("kick".equals(args[0])) {
                    completions.add("This is a test for kick");

                } else if("confer".equals(args[0])) {
                    completions.add("This is a test for confer");

                } else if("leaderboard".equals(args[0])) {
                    completions.addAll(Arrays.asList("bosskills", "experience", "kdr", "playtime"));
                } else if("list".equals(args[0])) {
                    completions.addAll(Arrays.asList("alpha", "creation"));
                } else if("roster".equals(args[0])) {
                    completions = plugin.getClanManager().getClans().stream().map(clan -> clan.getString("tag")).collect(Collectors.toList());
                }
                break;
            default: break;
        }
        return completions;
    }
}
