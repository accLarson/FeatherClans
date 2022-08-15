package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ListCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ListCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.list")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        List<String> clans = plugin.getClanManager().getClans();
        if (clans.isEmpty()) {
            player.sendMessage(messages.get("clan_list_no_clans", null));
            return true;
        }

        List<String> sortedClans = clans.stream().sorted(Comparator.comparingInt(clan -> plugin.getClanManager().getOfflinePlayersByClan(clan).size())).collect(Collectors.toList());
        sortedClans = sortedClans.stream().sorted(Comparator.comparingInt(clan -> (int) plugin.getClanManager().getOfflinePlayersByClan(clan).stream().filter(OfflinePlayer::isOnline).count())).collect(Collectors.toList());

        Collections.reverse(sortedClans);

        ChatUtil chatUtil = new ChatUtil(this.plugin);
        MiniMessage parser = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.reset()).build()).build();

        List<Component> clanLines = new ArrayList<>();

        Component header = chatUtil.addSpacing(parser.deserialize("<gray>Clan"),45)
                .append(chatUtil.addSpacing(parser.deserialize("<gray>Leader"),75))
                .append(chatUtil.addSpacing(parser.deserialize("<gray>Online"), 50, true))
                .append(chatUtil.addSpacing(parser.deserialize("<gray>Last Login"),140,true));

        clanLines.add(header);

        for (String clan : sortedClans) {
            List<OfflinePlayer> clanMembers = plugin.getClanManager().getOfflinePlayersByClan(clan);
            int lastSeenInt = clanMembers.stream().mapToInt(m -> (int) ((System.currentTimeMillis() - m.getLastLogin()) / 86400000)).min().getAsInt();

            Component tag = chatUtil.addSpacing(parser.deserialize(clan), 45)
                    .hoverEvent(HoverEvent.showText(Component.text("Click to view " + clan + " clan roster")))
                    .clickEvent(ClickEvent.runCommand("/clan roster " + clan));
            Component leader = chatUtil.addSpacing(parser.deserialize("<#949bd1>" + Bukkit.getOfflinePlayer(plugin.getClanManager().getLeader(clan)).getName()),75);
            Component online = chatUtil.addSpacing(parser.deserialize("<#6C719D>" + clanMembers.stream().filter(OfflinePlayer::isOnline).count() + "/" + clanMembers.size()),50,true);
            Component lastSeen;
            if (lastSeenInt == 0) lastSeen = chatUtil.addSpacing(parser.deserialize("<#6C719D>Today"),140,true);
            else lastSeen = chatUtil.addSpacing(parser.deserialize("<#6C719D>" + lastSeenInt + " Day(s) Ago"),140,true);

            clanLines.add(tag.append(leader).append(online).append(lastSeen));
        }

        plugin.getPaginateUtil().displayPage(args, (Player)sender, clanLines);
        return true;
    }
}