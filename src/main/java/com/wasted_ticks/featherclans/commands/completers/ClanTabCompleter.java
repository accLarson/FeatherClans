package com.wasted_ticks.featherclans.commands.completers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClanTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList(
            "accept",
            "chat",
            "confer",
            "create",
            "decline",
            "disband",
            "friendlyfire",
            "help",
            "home",
            "invite",
            "kick",
//            "leaderboard",
            "list",
            "resign",
            "roster",
            "sethome"
    );
    private final FeatherClans plugin;


    public ClanTabCompleter(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1:
                StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
                if(sender.isOp()) {
                    StringUtil.copyPartialMatches(args[0], List.of("banner"), completions);
                }
                break;
            case 2:
                if ("invite".equals(args[0])) {
                    if (this.plugin.getClanManager().isOfflinePlayerLeader((Player) sender)) {
                        completions = plugin.getServer().getOnlinePlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toList());
                    }
                } else if ("kick".equals(args[0])) {
                    if (this.plugin.getClanManager().isOfflinePlayerLeader((Player) sender)) {
                        String tag = this.plugin.getClanManager().getClanByOfflinePlayer((Player) sender);
                        completions = plugin.getClanManager().getOfflinePlayersByClan(tag).stream().map(OfflinePlayer::getName).collect(Collectors.toList());
                    }
                } else if ("confer".equals(args[0])) {
                    if (this.plugin.getClanManager().isOfflinePlayerLeader((Player) sender)) {
                        String tag = this.plugin.getClanManager().getClanByOfflinePlayer((Player) sender);
                        completions = plugin.getClanManager().getOfflinePlayersByClan(tag).stream().map(OfflinePlayer::getName).collect(Collectors.toList());
                    }
                } else if ("leaderboard".equals(args[0])) {
                    // exp: total of all offline players current exp
                    // hours: total of all offline players current hours
                    // kdr: mode, resets monthly
//                    completions.addAll(Arrays.asList("exp", "hours", "kdr"));
                } else if ("banner".equals(args[0])) {
                    if (sender.hasPermission("feather.clans.banner")) {
                        StringUtil.copyPartialMatches(args[1], plugin.getClanManager().getClans(), completions);
                    }
                } else if ("list".equals(args[0])) {
                    //tag
                    //creation date
                    //num of players
                    //last seen
//                    completions.addAll(Arrays.asList("alpha", "creation"));
                } else if ("roster".equals(args[0])) {
                    StringUtil.copyPartialMatches(args[1], plugin.getClanManager().getClans(), completions);
                    //name
                    //exp
                    //hours
                    //kdr
                    //last seen
//                    completions = plugin.getClanManager().getClans();
                }
                break;
            default:
                break;
        }
        return completions;
    }
}
