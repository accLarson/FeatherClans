package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.utilities.ChatUtility;
import com.wasted_ticks.featherclans.utilities.TimeUtility;
import net.kyori.adventure.text.event.HoverEvent;
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

        // Use ActiveManager to get active member count
        String activeCount = String.valueOf(plugin.getActiveManager().getActiveMemberCount(clanTag));

        // Sort clan members with a three-tier hierarchy:
        // 1. Leaders at the top
        // 2. Officers in the middle
        // 3. Regular members at the bottom
        // Within each tier, sort by last seen time (most recently active first)
        List<OfflinePlayer> sortedClanMembers = clanMembers.stream()
                .sorted(Comparator.comparingLong(m -> (System.currentTimeMillis() - m.getLastSeen())))
                .sorted(Comparator.comparing((OfflinePlayer m) -> !plugin.getClanManager().isOfflinePlayerOfficer(m))
                        .thenComparing(m -> !plugin.getClanManager().isOfflinePlayerLeader(m)))
                .collect(Collectors.toList());

        ChatUtility chatUtility = new ChatUtility(this.plugin);
        MiniMessage parser = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.reset()).build()).build();

        List<Component> rosterOutputLines = new ArrayList<>();

        Component clanInfoLine = parser.deserialize("<gray>Clan: <#949BD1>" + clanTag);
        clanInfoLine = chatUtility.addSpacing(clanInfoLine, 72);
        clanInfoLine = clanInfoLine.append(chatUtility.addSpacing(parser.deserialize("<gray>Ally: <#949BD1>-"), 72));
        clanInfoLine = clanInfoLine.append(chatUtility.addSpacing(parser.deserialize("<gray>Online: <#949BD1>" + onlineCount + "/" + clanMembers.size()), 96, true));

        // Create hover text for active members count
        int memberReq = plugin.getFeatherClansConfig().getClanActiveMembersRequirement();
        int dayReq = plugin.getFeatherClansConfig().getClanInactiveDaysThreshold();
        String hoverText = "<#7FD47F>Active clan status <#6C719D>requirement:\n" + "<white>" + memberReq + "+ <#6C719D>members seen within <white>" + dayReq + " <#6C719D>days";


        // Apply conditional coloring based on active status
        String activeValueColor = "<gray>";
        if (activeCount.equals("0")) activeValueColor = "<dark_gray>";
        else if (plugin.getActiveManager().isActive(clanTag)) activeValueColor = "<#7FD47F>"; // Pastel green color
        Component activeComponent = parser.deserialize("<gray>Active: " + activeValueColor + activeCount).hoverEvent(HoverEvent.showText(parser.deserialize(hoverText)));

        clanInfoLine = clanInfoLine.append(chatUtility.addSpacing(activeComponent, 70, true));
        rosterOutputLines.add(clanInfoLine);
        rosterOutputLines.add(messages.get("clan_line", null));

        Component header = chatUtility.addSpacing(parser.deserialize("<gray>Member"), 120)
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Role"), 80))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Seen"), 110, true));

        rosterOutputLines.add(header);

        for (OfflinePlayer clanMember : sortedClanMembers) {
            String name = "null";
            if (clanMember.getName() != null) name = clanMember.getName();

            Component member;
            member = chatUtility.addSpacing(parser.deserialize(name), 120);

            Component role;
            if (manager.isOfflinePlayerLeader(clanMember)) {
                role = chatUtility.addSpacing(parser.deserialize("<#6C719D>Leader"), 80);
            } else if (manager.isOfflinePlayerOfficer(clanMember)) {
                role = chatUtility.addSpacing(parser.deserialize("<#6C719D>Officer"), 80);
            } else {
                role = chatUtility.addSpacing(parser.deserialize("<#6C719D>Member"), 80);
            }
            
            Component lastSeen;
            String lastSeenText = TimeUtility.formatTimeSince(clanMember.getLastSeen());
            
            // Check if player is an alt account
            if (plugin.getAltUtility().isAlt(clanMember)) {
                // Add asterisk prefix and hover text for alt accounts
                String hoverText2 = "<#949BD1>This is an alt account and is not included in the active count.";
                Component altLastSeen = parser.deserialize("<#6C719D>*" + lastSeenText)
                        .hoverEvent(HoverEvent.showText(parser.deserialize(hoverText2)));
                lastSeen = chatUtility.addSpacing(altLastSeen, 110, true);
            } else {
                lastSeen = chatUtility.addSpacing(parser.deserialize("<#6C719D>" + lastSeenText), 110, true);
            }
            
            rosterOutputLines.add(member.append(role).append(lastSeen));
        }

        plugin.getPaginateUtil().displayPage(args, (Player) sender, rosterOutputLines);

        return true;
    }
}
