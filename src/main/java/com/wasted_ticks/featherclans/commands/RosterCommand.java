package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.utilities.ChatUtility;
import com.wasted_ticks.featherclans.utilities.TimeUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RosterCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public RosterCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.roster")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        String clanTag = null;
        ClanManager manager = plugin.getClanManager();

        if (args.length == 1) {
            if (manager.isOfflinePlayerInClan((OfflinePlayer) sender)) {
                clanTag = manager.getClanByOfflinePlayer((OfflinePlayer) sender);
                String[] newArgs = Arrays.copyOf(args, args.length + 1);
                newArgs[args.length] = clanTag;
                args = newArgs;
            } else {
                sender.sendMessage(messages.get("clan_roster_error_unresolved_clan", null));
                return true;
            }
        }

        if (clanTag == null) clanTag = args[1];

        if (manager.getClans().stream().noneMatch(clanTag::equalsIgnoreCase)) {
            sender.sendMessage(messages.get("clan_roster_error_unresolved_clan", null));
            return true;
        }

        List<OfflinePlayer> clanMembers = manager.getOfflinePlayersByClan(clanTag.toLowerCase());
        int onlineCount = (int) clanMembers.stream().filter(member -> member.isOnline() && !isVanished(member.getPlayer())).count();
        String activeMembers = "x";

        List<OfflinePlayer> sortedClanMembers = clanMembers.stream().sorted(Comparator.comparingLong(m -> (System.currentTimeMillis() - m.getLastSeen()))).collect(Collectors.toList());
        sortedClanMembers = sortedClanMembers.stream().sorted(Comparator.comparing(m -> !plugin.getClanManager().isOfflinePlayerLeader(m))).collect(Collectors.toList());

        ChatUtility chatUtility = new ChatUtility(this.plugin);
        MiniMessage parser = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.reset()).build()).build();

        List<Component> rosterOutputLines = new ArrayList<>();

        Component clanInfoLine = parser.deserialize("<gray>Clan: <#949BD1>" + clanTag);
        clanInfoLine = chatUtility.addSpacing(clanInfoLine, 72);
        clanInfoLine = clanInfoLine.append(chatUtility.addSpacing(parser.deserialize("<gray>Partner: <#949BD1>soon"), 72));
        clanInfoLine = clanInfoLine.append(chatUtility.addSpacing(parser.deserialize("<gray>Online: <#949BD1>" + onlineCount + "/" + clanMembers.size()), 96, true));
        clanInfoLine = clanInfoLine.append(chatUtility.addSpacing(parser.deserialize("<gray>Active: <#949BD1>" + activeMembers), 70, true));
        rosterOutputLines.add(clanInfoLine);
        rosterOutputLines.add(messages.get("clan_line", null));

        Component header = chatUtility.addSpacing(parser.deserialize("<gray>Member"), 120)
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Role"), 80))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Last Seen"), 110, true));

        rosterOutputLines.add(header);

        for (OfflinePlayer clanMember : sortedClanMembers) {
            String name = "null";
            if (clanMember.getName() != null) name = clanMember.getName();

            Component member;
            member = chatUtility.addSpacing(parser.deserialize(name), 120);

            Component role;
            if (manager.isOfflinePlayerLeader(clanMember)) {
                role = chatUtility.addSpacing(parser.deserialize("<#6C719D>Leader"), 80);
            } else {
                role = chatUtility.addSpacing(parser.deserialize("<#6C719D>Member"), 80);
            }

            Component lastSeen;
            lastSeen = chatUtility.addSpacing(parser.deserialize("<#6C719D>" + TimeUtility.formatTimeSince(clanMember.getLastSeen())), 110, true);
            rosterOutputLines.add(member.append(role).append(lastSeen));
        }

        plugin.getPaginateUtil().displayPage(args, (Player) sender, rosterOutputLines);

        return true;
    }
}
