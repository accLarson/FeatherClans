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
            "settag",
            "ally"
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

        // Handle /clan manage separately - it has its own argument structure
        if (args.length > 0 && args[0].equalsIgnoreCase("manage") && sender.hasPermission("feather.clans.manage")) {
            handleManageTabCompletion(sender, args, completions);
            return completions;
        }

        // Handle /cc and /cac - return an empty list to suppress player name suggestions
        if (alias.equalsIgnoreCase("cc") || alias.equalsIgnoreCase("cac")) {
            // Return the empty list - no tab completion for chat messages
            return completions;
        }
        
        // Handle regular /clan commands
        // Handle /cmcc tab completion - show clan names at position 0
        if (alias.equalsIgnoreCase("cmcc")) {
            if (args.length == 1) {
                StringUtil.copyPartialMatches(args[0], manager.getClans(), completions);
            }
            // No completion for message arguments
            return completions;
        }


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
                            StringUtil.copyPartialMatches(args[1], plugin.getServer().getOnlinePlayers().stream()
                                    .filter(p -> !manager.isOfflinePlayerInClan(p))
                                    .map(Player::getName)
                                    .collect(Collectors.toList()), completions);
                        }
                        break;

                    case "kick":
                        if (manager.isOfflinePlayerLeader((Player) sender) || manager.isOfflinePlayerOfficer((Player) sender)) {
                            String tag = manager.getClanByOfflinePlayer((Player) sender);
                            StringUtil.copyPartialMatches(args[1], manager.getOfflinePlayersByClan(tag).stream()
                                    .map(OfflinePlayer::getName)
                                    .collect(Collectors.toList()), completions);
                        }
                        break;

                    case "confer":
                        if (manager.isOfflinePlayerLeader((Player) sender)) {
                            String tag = manager.getClanByOfflinePlayer((Player) sender);
                            StringUtil.copyPartialMatches(args[1], manager.getOfflinePlayersByClan(tag).stream()
                                    .map(OfflinePlayer::getName)
                                    .collect(Collectors.toList()), completions);
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

                    case "ally":
                        if (manager.isOfflinePlayerLeader((Player) sender)) {
                            StringUtil.copyPartialMatches(args[1], List.of("propose", "dissolve"), completions);
                        }
                        break;
                        
                    case "debug":
                        if (sender.hasPermission("feather.clans.debug")) StringUtil.copyPartialMatches(args[1], List.of("updatedisplay","getactive"), completions);
                        break;

                }
                break;

            case 3:
                // Handle /clan officer [promote|demote] [player]
                if (manager.isOfflinePlayerLeader((Player) sender) && args[0].equalsIgnoreCase("officer")) {
                    String tag = manager.getClanByOfflinePlayer((Player) sender);
                    StringUtil.copyPartialMatches(args[2], manager.getOfflinePlayersByClan(tag).stream()
                            .map(OfflinePlayer::getName)
                            .collect(Collectors.toList()), completions);
                }
                
                // Handle /clan ally propose [clan]
                if (manager.isOfflinePlayerLeader((Player) sender) && args[0].equalsIgnoreCase("ally") && args[1].equalsIgnoreCase("propose")) {
                    String tag = manager.getClanByOfflinePlayer((Player) sender);
                    StringUtil.copyPartialMatches(args[2], manager.getClans().stream()
                            .filter(clan -> !clan.equalsIgnoreCase(tag))
                            .collect(Collectors.toList()), completions);
                }
                break;

            case 4:
                // Handle /clan officer [promote|demote] [player] confirm
                if (manager.isOfflinePlayerLeader((Player) sender) && args[0].equalsIgnoreCase("officer")) {
                    StringUtil.copyPartialMatches(args[3], List.of("confirm"), completions);
                }
                
                // Handle /clan ally propose [clan] confirm
                if (manager.isOfflinePlayerLeader((Player) sender) && args[0].equalsIgnoreCase("ally") && args[1].equalsIgnoreCase("propose")) {
                    StringUtil.copyPartialMatches(args[3], List.of("confirm"), completions);
                }
                
                // Handle /clan ally dissolve confirm
                if (manager.isOfflinePlayerLeader((Player) sender) && args[0].equalsIgnoreCase("ally") && args[1].equalsIgnoreCase("dissolve")) {
                    StringUtil.copyPartialMatches(args[2], List.of("confirm"), completions);
                }
                break;

            default:
                break;
        }
        return completions;
    }
    
    /**
     * Handles tab completion for /clan manage commands separately from regular player commands.
     * Manage commands have a different argument structure: /clan manage [clan] [action] [args...]
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @param completions The list to add completions to
     */
    private void handleManageTabCompletion(CommandSender sender, String[] args, List<String> completions) {
        switch (args.length) {
            case 2:
                // /clan manage [clan] - show all clan names
                StringUtil.copyPartialMatches(args[1], manager.getClans(), completions);
                break;
                
            case 3:
                // /clan manage [clan] [action] - show available manage actions
                StringUtil.copyPartialMatches(args[2], List.of("chat", "confer", "invite", "kick", "disband", "sethome", "officer", "setarmor", "setbanner", "settag", "ally"), completions);
                break;
                
            case 4:
                // /clan manage [clan] [action] [arg] - context-specific completions
                String clanTag = args[1];
                switch (args[2].toLowerCase()) {
                    case "kick":
                    case "confer":
                        // Show clan members
                        StringUtil.copyPartialMatches(args[3], manager.getOfflinePlayersByClan(clanTag).stream()
                                .map(OfflinePlayer::getName)
                                .collect(Collectors.toList()), completions);
                        break;
                        
                    case "invite":
                        // Show online players not in any clan
                        StringUtil.copyPartialMatches(args[3], plugin.getServer().getOnlinePlayers().stream()
                                .filter(p -> !manager.isOfflinePlayerInClan(p))
                                .map(Player::getName)
                                .collect(Collectors.toList()), completions);
                        break;
                        
                    case "officer":
                        // Show promote/demote options
                        StringUtil.copyPartialMatches(args[3], List.of("promote", "demote"), completions);
                        break;
                        
                    case "ally":
                        // Show propose/dissolve options
                        StringUtil.copyPartialMatches(args[3], List.of("propose", "dissolve"), completions);
                        break;
                        
                    case "settag":
                    case "chat":
                    case "disband":
                    case "sethome":
                    case "setarmor":
                    case "setbanner":
                        // No completion needed for these
                        break;
                }
                break;
                
            case 5:
                // /clan manage [clan] [action] [subaction] [arg]
                String targetClan = args[1];
                if (args[2].equalsIgnoreCase("officer")) {
                    // /clan manage [clan] officer [promote|demote] [player]
                    StringUtil.copyPartialMatches(args[4], manager.getOfflinePlayersByClan(targetClan).stream()
                            .map(OfflinePlayer::getName)
                            .collect(Collectors.toList()), completions);
                } else if (args[2].equalsIgnoreCase("ally") && args[3].equalsIgnoreCase("propose")) {
                    // /clan manage [clan] ally propose [clan]
                    StringUtil.copyPartialMatches(args[4], manager.getClans().stream()
                            .filter(clan -> !clan.equalsIgnoreCase(targetClan))
                            .collect(Collectors.toList()), completions);
                }
                break;
                
            default:
                // No completions for deeper arguments
                break;
        }
    }
}
