package com.wasted_ticks.featherclans.commands.completers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.managers.ClanManager;
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

    private static final List<String> EVERYONE_COMMANDS = Arrays.asList(
            "accept",
            "create",
            "decline",
            "help",
            // "leaderboard",
            "list",
            "roster"
    );
    private static final List<String> MEMBER_COMMANDS = Arrays.asList(
            "chat",
            "friendlyfire",
            "help",
            "home",
            // "leaderboard",
            "list",
            "resign",
            "roster"
    );
    private static final List<String> LEADER_COMMANDS = Arrays.asList(
            "chat",
            "confer",
            "disband",
            "friendlyfire",
            "help",
            "home",
            "invite",
            "kick",
            // "leaderboard",
            "list",
            "resign",
            "roster",
            "sethome"
    );
    private final FeatherClans plugin;
    private final ClanManager manager;


    public ClanTabCompleter(FeatherClans plugin) {
        this.plugin = plugin;
        this.manager = this.plugin.getClanManager();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();


        switch (args.length) {
            case 1:
                if (manager.isOfflinePlayerLeader((OfflinePlayer) sender)) StringUtil.copyPartialMatches(args[0], LEADER_COMMANDS, completions);

                else if (manager.isOfflinePlayerInClan((OfflinePlayer) sender)) StringUtil.copyPartialMatches(args[0], MEMBER_COMMANDS, completions);

                else StringUtil.copyPartialMatches(args[0], EVERYONE_COMMANDS, completions);

                if(sender.isOp()) StringUtil.copyPartialMatches(args[0], List.of("banner"), completions);

                break;

            case 2:
                switch (args[0]) {
                    case "invite":
                        if (manager.isOfflinePlayerLeader((Player) sender)) {
                            StringUtil.copyPartialMatches(args[1], plugin.getServer().getOnlinePlayers().stream().filter(p -> !manager.isOfflinePlayerInClan(p)).map(Player::getName).collect(Collectors.toList()), completions);
                        }
                        break;

                    case "kick":
                        if (manager.isOfflinePlayerLeader((Player) sender)) {
                            String tag = manager.getClanByOfflinePlayer((Player) sender);
                            StringUtil.copyPartialMatches(args[1], manager.getOfflinePlayersByClan(tag).stream().map(OfflinePlayer::getName).collect(Collectors.toList()), completions);
                        }
                        break;

                    case "confer":
                        if (manager.isOfflinePlayerLeader((Player) sender)) {
                            String tag = manager.getClanByOfflinePlayer((Player) sender);
                            completions = manager.getOfflinePlayersByClan(tag).stream().map(OfflinePlayer::getName).collect(Collectors.toList());
                        }
                        break;

                    case "banner":
                        if (sender.hasPermission("feather.clans.banner")) StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                        break;

                    case "home":
                        if (sender.hasPermission("feather.clans.home.others")) StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                        break;

                    case "roster":
                        if (sender.hasPermission("feather.clans.roster")) StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                        // name
                        // exp
                        // hours
                        // kdr
                        // last seen
                        // completions = manager.getClans();
                        break;

                    case "leaderboard":
                        // exp: total of all offline players current exp
                        // hours: total of all offline players current hours
                        // kdr: mode, resets monthly
                        // completions.addAll(Arrays.asList("exp", "hours", "kdr"));
                        break;

                    case "list":
                        // tag
                        // creation date
                        // num of players
                        // last seen
                        // completions.addAll(Arrays.asList("alpha", "creation"));
                        break;
                }
                break;
            default:
                break;
        }
        return completions;
    }
}
