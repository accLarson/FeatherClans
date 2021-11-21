package com.wasted_ticks.featherclans.commands.completers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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
        "invite",
        "kick",
        "disband",
        "confer",
        "chat",
        "accept",
        "decline",
        "help",
        "leaderboard",
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

        switch(args.length) {
            case 1:
                StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
                break;
            case 2:
                if("invite".equals(args[0])) {
                    if(this.plugin.getClanManager().isOfflinePlayerLeader((Player) sender)) {
                        completions = plugin.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
                    }
                } else if("kick".equals(args[0])) {
                    if(this.plugin.getClanManager().isOfflinePlayerLeader((Player) sender)) {
                        Clan clan = this.plugin.getClanManager().getClanByOfflinePlayer((Player) sender);
                        completions = plugin.getClanManager().getOfflinePlayersByClan(clan).stream().map(player -> player.getName()).collect(Collectors.toList());
                    }
                } else if("confer".equals(args[0])) {
                    if(this.plugin.getClanManager().isOfflinePlayerLeader((Player) sender)) {
                        Clan clan = this.plugin.getClanManager().getClanByOfflinePlayer((Player) sender);
                        completions = plugin.getClanManager().getOfflinePlayersByClan(clan).stream().map(player -> player.getName()).collect(Collectors.toList());
                    }
                } else if("leaderboard".equals(args[0])) {
                    // exp: total of all offline players current exp
                    // hours: total of all offline players current hours
                        // kdr: mode, resets monthly
                    completions.addAll(Arrays.asList("exp", "hours", "kdr"));
                } else if("list".equals(args[0])) {
                    //tag
                    //creation date
                    //num of players
                    //last seen
                    completions.addAll(Arrays.asList("alpha", "creation"));
                } else if("roster".equals(args[0])) {
                    //name
                    //exp
                    //hours
                    //kdr
                    //last seen
                    plugin.getServer().shutdown();
                    completions = plugin.getClanManager().getClans().stream().map(clan -> clan.getString("tag")).collect(Collectors.toList());
                }
                break;
            default: break;
        }
        return completions;
    }
}
