package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class RosterCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public RosterCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
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
                String[] newArgs = Arrays.copyOf(args,args.length+1);
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

        List<OfflinePlayer> sortedClanMembers = clanMembers.stream().sorted(Comparator.comparingLong(m -> (System.currentTimeMillis() - m.getLastSeen()))).collect(Collectors.toList());
        sortedClanMembers = sortedClanMembers.stream().sorted(Comparator.comparing(m -> !plugin.getClanManager().isOfflinePlayerLeader(m))).collect(Collectors.toList());

        DecimalFormat df = new DecimalFormat("0.00");
        ChatUtil chatUtil = new ChatUtil(this.plugin);
        MiniMessage parser = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.reset()).build()).build();

        List<Component> clanMemberLines = new ArrayList<>();

        Component header = chatUtil.addSpacing(parser.deserialize("<gray>Member"),75)
                .append(chatUtil.addSpacing(parser.deserialize("<gray>KDR"),55,true))
                .append(chatUtil.addSpacing(parser.deserialize("<gray>Hours"),55,true))
                .append(chatUtil.addSpacing(parser.deserialize("<gray>Last Seen"),125,true));

        clanMemberLines.add(header);

        for (OfflinePlayer clanMember : sortedClanMembers) {
            int lastSeenInt = (int) ((System.currentTimeMillis() - clanMember.getLastLogin()) / 86400000);
            double kdr = (double) clanMember.getStatistic(Statistic.PLAYER_KILLS) / clanMember.getStatistic(Statistic.DEATHS);
            String name = "null";
            if (clanMember.getName() != null) name = clanMember.getName();

            Component member;
            if (manager.isOfflinePlayerLeader(clanMember)) member = chatUtil.addSpacing(parser.deserialize(name + " <dark_gray>(L)"), 75);
            else member = chatUtil.addSpacing(parser.deserialize(name), 75);

            Component KDR;
            if (clanMember.isOnline()) KDR = chatUtil.addSpacing(parser.deserialize("<#6C719D>" + df.format(kdr)),55,true);
            else KDR = chatUtil.addSpacing(parser.deserialize("<#6C719D>Offline"),55,true);

            Component hours;
            if (clanMember.isOnline()) hours = chatUtil.addSpacing(parser.deserialize("<#6C719D>" + clanMember.getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60/60),55,true);
            else hours = chatUtil.addSpacing(parser.deserialize("<#6C719D>Offline"),55,true);

            Component lastSeen;
            if (lastSeenInt == 0) lastSeen = chatUtil.addSpacing(parser.deserialize("<#6C719D>Today"),125,true);
            else lastSeen = chatUtil.addSpacing(parser.deserialize("<#6C719D>" + lastSeenInt + " Day(s) Ago"),125,true);

            clanMemberLines.add(member.append(KDR).append(hours).append(lastSeen));
        }

        plugin.getPaginateUtil().displayPage(args, (Player)sender, clanMemberLines);

        return true;
    }
}
