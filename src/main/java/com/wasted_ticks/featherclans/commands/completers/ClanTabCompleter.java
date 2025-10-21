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
            "list",
            "roster"
    );
    private static final List<String> MEMBER_COMMANDS = Arrays.asList(
            "help",
            "list",
            "roster",
            // additional
            "chat",
            "allychat",
            "friendlyfire",
            "home",
            "resign"
    );
    private static final List<String> OFFICER_COMMANDS = Arrays.asList(
            "help",
            "list",
            "roster",
            "chat",
            "allychat",
            "friendlyfire",
            "home",
            "resign",
            // additional
            "invite",
            "kick",
            "sethome",
            "takeover" // OFFICER ONLY - NOT AVAILABLE TO LEADER
    );
    private static final List<String> LEADER_COMMANDS = Arrays.asList(
            "chat",
            "allychat",
            "friendlyfire",
            "help",
            "home",
            "list",
            "resign",
            "roster",
            "invite",
            "kick",
            "sethome",
            // additional
            "confer",
            "disband",
            "officer",
            "setarmor",
            "setbanner",
            "settag"
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

                else if (manager.isOfflinePlayerOfficer((OfflinePlayer) sender)) StringUtil.copyPartialMatches(args[0], OFFICER_COMMANDS, completions);

                else if (manager.isOfflinePlayerInClan((OfflinePlayer) sender)) StringUtil.copyPartialMatches(args[0], MEMBER_COMMANDS, completions);

                else StringUtil.copyPartialMatches(args[0], EVERYONE_COMMANDS, completions);

                if(sender.isOp()) StringUtil.copyPartialMatches(args[0], List.of("banner","manage", "debug"), completions);
                if(manager.isOfflinePlayerLeader((OfflinePlayer) sender)) StringUtil.copyPartialMatches(args[0], List.of("ally"), completions);

                break;

            case 2:
                switch (args[0]) {
                    case "invite":
                        if (manager.isOfflinePlayerLeader((Player) sender) || manager.isOfflinePlayerInClan((Player) sender)) {
                            StringUtil.copyPartialMatches(args[1], plugin.getServer().getOnlinePlayers().stream().filter(p -> !manager.isOfflinePlayerInClan(p)).map(Player::getName).collect(Collectors.toList()), completions);
                        }
                        break;

                    case "kick":
                        if (manager.isOfflinePlayerLeader((Player) sender) || manager.isOfflinePlayerOfficer((Player) sender)) {
                            String tag = manager.getClanByOfflinePlayer((Player) sender);
                            StringUtil.copyPartialMatches(args[1], manager.getOfflinePlayersByClan(tag).stream().map(OfflinePlayer::getName).collect(Collectors.toList()), completions);
                        }
                        break;

                    case "confer":
                        if (manager.isOfflinePlayerLeader((Player) sender)) {
                            String tag = manager.getClanByOfflinePlayer((Player) sender);
                            StringUtil.copyPartialMatches(args[1], manager.getOfflinePlayersByClan(tag).stream().map(OfflinePlayer::getName).collect(Collectors.toList()), completions);
                        }
                        break;

                    case "officer":
                        if (manager.isOfflinePlayerLeader((Player) sender)) StringUtil.copyPartialMatches(args[1], List.of("promote","demote"), completions);
                        break;

                    case "banner":
                        if (sender.hasPermission("feather.clans.banner")) StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                        break;

                    case "home":
                        if (sender.hasPermission("feather.clans.home.others")) StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                        break;

                    case "roster":
                        if (sender.hasPermission("feather.clans.roster")) StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                        break;

                    case "manage":
                        if (sender.hasPermission("feather.clans.manage")) StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                        break;
                    case "ally":
                        if (manager.isOfflinePlayerLeader((Player) sender)) {
                            StringUtil.copyPartialMatches(args[1], List.of("propose", "dissolve"), completions);
                        }
                        break;
                    case "debug":
                        if (sender.hasPermission("feather.clans.debug")) StringUtil.copyPartialMatches(args[1], List.of("updatedisplay","getactive"), completions);

                }
                break;

            case 3:
                if (manager.isOfflinePlayerLeader((Player) sender) && args[0].equalsIgnoreCase("ally") && args[1].equalsIgnoreCase("propose")) {
                    String tag = manager.getClanByOfflinePlayer((Player) sender);
                    StringUtil.copyPartialMatches(args[2], manager.getClans().stream()
                            .filter(clan -> !clan.equalsIgnoreCase(tag))
                            .collect(Collectors.toList()), completions);
                }
                break;

            case 4:
                if (sender.hasPermission("feather.clans.manage") && args[0].equalsIgnoreCase("manage")) {
                    switch (args[2]) {

                        case "kick":
                        case "confer":
                            String tag = args[1];
                            StringUtil.copyPartialMatches(args[3], manager.getOfflinePlayersByClan(tag).stream().map(OfflinePlayer::getName).collect(Collectors.toList()), completions);
                            break;

                        case "invite":
                            StringUtil.copyPartialMatches(args[3], plugin.getServer().getOnlinePlayers().stream().filter(p -> !manager.isOfflinePlayerInClan(p)).map(Player::getName).collect(Collectors.toList()), completions);
                            break;
                    }
                }

            default:
                break;
        }
        return completions;
    }
}
