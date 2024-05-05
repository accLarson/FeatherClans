package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.utilities.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ListCommand(FeatherClans plugin) {
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

        if (!sender.hasPermission("feather.clans.list")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        ClanManager manager = plugin.getClanManager();
        List<String> clans = manager.getClans();

        if (clans.isEmpty()) {
            player.sendMessage(messages.get("clan_list_no_clans", null));
            return true;
        }

        List<String> sortedClans = clans.stream().sorted(Comparator.comparingInt(clan -> manager.getOfflinePlayersByClan(clan).size())).collect(Collectors.toList());
        sortedClans = sortedClans.stream().sorted(Comparator.comparingInt(clan -> (int) manager.getOfflinePlayersByClan(clan).stream().filter(OfflinePlayer::isOnline).count())).collect(Collectors.toList());

        Collections.reverse(sortedClans);

        ChatUtil chatUtil = new ChatUtil(this.plugin);
        MiniMessage mm = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.reset()).build()).build();

        List<Component> clanLines = new ArrayList<>();

        Component header = chatUtil.addSpacing(mm.deserialize("<gray>Clan"),40)
                .append(chatUtil.addSpacing(mm.deserialize("<gray>Leader"),100))
                .append(chatUtil.addSpacing(mm.deserialize("<gray>Partner"), 40))
                .append(chatUtil.addSpacing(mm.deserialize("<gray>Online"), 42, true))
                .append(chatUtil.addSpacing(mm.deserialize("<gray>Active"), 42, true))
                .append(chatUtil.addSpacing(mm.deserialize("<gray>Seen"),48,true));

        clanLines.add(header);

        for (String clan : sortedClans) {
            List<OfflinePlayer> clanMembers = manager.getOfflinePlayersByClan(clan);
            int lastSeenInt = clanMembers.stream().mapToInt(m -> (int) ((System.currentTimeMillis() - m.getLastLogin()) / 86400000)).min().getAsInt();

            Component tag = chatUtil.addSpacing(mm.deserialize(clan), 40);
            Component leader = chatUtil.addSpacing(mm.deserialize("<#949bd1>" + Bukkit.getOfflinePlayer(manager.getLeader(clan)).getName()),100);
            Component partner = chatUtil.addSpacing(mm.deserialize("<#949bd1>" + "todo"),40);
            Component online = chatUtil.addSpacing(mm.deserialize("<#949BD1>" + clanMembers.stream().filter(member -> (member.isOnline() && !this.isVanished(member.getPlayer()))).count() + "/" + clanMembers.size()),42,true);
            Component active = chatUtil.addSpacing(mm.deserialize("<#949BD1>" + manager.getClanSize(clan,true) + "/" + manager.getClanSize(clan,false)),42,true);

            Component lastSeen;
            if (lastSeenInt == 0) lastSeen = chatUtil.addSpacing(mm.deserialize("<#949BD1>Today"),48,true);
            else lastSeen = chatUtil.addSpacing(mm.deserialize("<#949BD1>" + lastSeenInt + " d"),48,true);

            clanLines.add(tag.append(leader).append(partner).append(online).append(active).append(lastSeen)
                    .hoverEvent(HoverEvent.showText(mm.deserialize("<#949BD1>Click to view <white>" + clan + " <#949BD1>clan roster")))
                    .clickEvent(ClickEvent.runCommand("/clan roster " + clan)));
        }

        plugin.getPaginateUtil().displayPage(args, (Player)sender, clanLines);
        return true;
    }
}