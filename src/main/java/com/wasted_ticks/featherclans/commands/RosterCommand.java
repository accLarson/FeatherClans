package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class RosterCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final FeatherClansConfig config;

    public RosterCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.config = plugin.getFeatherClansConfig();
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
        sortedClanMembers = sortedClanMembers.stream().sorted(Comparator.comparing(m -> !manager.isOfflinePlayerActive(m))).collect(Collectors.toList());
        sortedClanMembers = sortedClanMembers.stream().sorted(Comparator.comparing(m -> !manager.isOfflinePlayerOfficer(m))).collect(Collectors.toList());
        sortedClanMembers = sortedClanMembers.stream().sorted(Comparator.comparing(m -> !manager.isOfflinePlayerLeader(m))).collect(Collectors.toList());

        ChatUtil chatUtil = new ChatUtil(this.plugin);
        MiniMessage mm = MiniMessage.miniMessage();

        List<Component> clanMemberLines = new ArrayList<>();

        Component pvpScoreCalculationExplained = mm.deserialize(config.getPVPScoreCalculationExplained(), Placeholder.parsed("days",String.valueOf(config.getPVPScoreRelevantDays())));

        Component header = chatUtil.addSpacing(mm.deserialize("<gray>Member"),140)
                .append(chatUtil.addSpacing(Component.text(" "),12))
                .append(chatUtil.addSpacing(mm.deserialize("<gray>[i] PVP"),36,true).hoverEvent(HoverEvent.showText(pvpScoreCalculationExplained)))
                .append(chatUtil.addSpacing(Component.text(" "),12))
                .append(chatUtil.addSpacing(mm.deserialize("<gray>Last Seen"),100,true));

        clanMemberLines.add(header);

        for (OfflinePlayer clanMember : sortedClanMembers) {

            int lastSeenInt = (int) ((System.currentTimeMillis() - clanMember.getLastLogin()) / 86400000);

            int pvpScoreInt = plugin.getPVPScoreManager().getScore(clanMember);

            List<Component> pvpScoreBreakdownLines = new ArrayList<>();

            pvpScoreBreakdownLines.add(mm.deserialize("<gray>PVP Score Breakdown"));

            String finalClanTag = clanTag;
            plugin.getPVPScoreManager().getKills(clanMember).forEach((killed, killCount) -> {
                int killedCount = plugin.getPVPScoreManager().getKills(killed).getOrDefault(clanMember,0);
                String killedClan = manager.getClanByOfflinePlayer(killed);
                int score = killCount > killedCount ? 1 : 0;
                Component pvpScoreHoverLine = mm.deserialize("<br>")
                        .append(chatUtil.addSpacing(mm.deserialize("<white>" + finalClanTag + " <#656B96>" + clanMember.getName()  + " <gray>vs " + "<white>" + killedClan + " <#656B96>" + killed.getName()), 180))
                        .append(chatUtil.addSpacing(mm.deserialize("<#949BD1>" + killCount + ":" + killedCount),46, true))
                        .append(chatUtil.addSpacing(mm.deserialize("<gold>" + score),20,true));
                pvpScoreBreakdownLines.add(pvpScoreHoverLine);
            });
            Component pvpScoreBreakdownComponent = Component.text("");
            for (Component line : pvpScoreBreakdownLines)
                pvpScoreBreakdownComponent = pvpScoreBreakdownComponent.append(line);

            String name = "null";

            if (clanMember.getName() != null) name = clanMember.getName();
            if (!manager.isOfflinePlayerActive(clanMember)) name = "<dark_gray><italic>" + name + "</italic>";

            Component member;
            if (manager.isOfflinePlayerLeader(clanMember)) member = chatUtil.addSpacing(mm.deserialize(name + " <#656b96>Leader").hoverEvent(HoverEvent.showText(Component.text("This player is the clan leader."))), 140);
            else if (manager.isOfflinePlayerOfficer(clanMember)) member = chatUtil.addSpacing(mm.deserialize(name + " <#656b96>Officer").hoverEvent(HoverEvent.showText(Component.text("This player is a clan officer."))), 140);
            else if (!manager.isOfflinePlayerActive(clanMember)) member = chatUtil.addSpacing(mm.deserialize(name + " <dark_gray><italic>Inactive</italic>").hoverEvent(HoverEvent.showText(Component.text("This player is inactive and wont be be counted when calculating active membership count for elevated status."))), 140);
            else member = chatUtil.addSpacing(mm.deserialize(name), 140);

            Component pvpScore = chatUtil.addSpacing(mm.deserialize("<#949BD1>" + pvpScoreInt),36,true)
                    .hoverEvent(HoverEvent.showText(pvpScoreBreakdownComponent));

            Component spacer = chatUtil.addSpacing(Component.text(" "),12);

            Component lastSeen;
            if (lastSeenInt == 0) lastSeen = chatUtil.addSpacing(mm.deserialize("<#949BD1>Today"),100,true);
            else lastSeen = chatUtil.addSpacing(mm.deserialize("<#949BD1>" + lastSeenInt + " Day(s) Ago"),100,true);

            clanMemberLines.add(member.append(spacer).append(pvpScore).append(spacer).append(lastSeen));
        }

        plugin.getPaginateUtil().displayPage(args, (Player)sender, clanMemberLines);

        return true;
    }
}
