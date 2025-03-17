package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.ChatUtility;
import com.wasted_ticks.featherclans.utilities.TimeUtility;
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

import java.util.*;
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
        List<String> clans = plugin.getClanManager().getClans();

        if (clans.isEmpty()) {
            player.sendMessage(messages.get("clan_list_no_clans", null));
            return true;
        }

        List<String> sortedClans = clans.stream().sorted(Comparator.comparingInt(clan -> plugin.getClanManager().getOfflinePlayersByClan(clan).size())).collect(Collectors.toList());
        sortedClans = sortedClans.stream().sorted(Comparator.comparingInt(clan -> (int) plugin.getClanManager().getOfflinePlayersByClan(clan).stream().filter(OfflinePlayer::isOnline).count())).collect(Collectors.toList());

        Collections.reverse(sortedClans);

        ChatUtility chatUtility = new ChatUtility(this.plugin);
        MiniMessage parser = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.reset()).build()).build();

        List<Component> clanLines = new ArrayList<>();

        Component header = chatUtility.addSpacing(parser.deserialize("<gray>Clan"), 40)
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Leader"), 105))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Partner"), 50))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Online"), 40))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Active"), 30))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Seen"), 50, true));

        clanLines.add(header);
        clanLines.add(messages.get("clan_line", null));

        for (String clan : sortedClans) {
            List<OfflinePlayer> clanMembers = plugin.getClanManager().getOfflinePlayersByClan(clan);
            // Get the most recent login time from clan members
            long mostRecentLogin = clanMembers.stream().mapToLong(OfflinePlayer::getLastLogin).max().orElse(0);

            Component tag = chatUtility.addSpacing(parser.deserialize(clan), 40);
            Component leader = chatUtility.addSpacing(parser.deserialize("<#949bd1>" + Bukkit.getOfflinePlayer(plugin.getClanManager().getLeader(clan)).getName()), 105);
            Component partner = chatUtility.addSpacing(parser.deserialize("<#949bd1>soon"), 50);
            Component online = chatUtility.addSpacing(parser.deserialize("<#6C719D>" + clanMembers.stream().filter(member -> (member.isOnline() && !this.isVanished((Player) member))).count() + "/" + clanMembers.size()), 40);
            
            // Use ActiveManager to get active member count
            String activeCount = String.valueOf(plugin.getActiveManager().getActiveCount(clan));
            
            Component active = chatUtility.addSpacing(parser.deserialize("<#6C719D>" + activeCount), 30);
            Component seen = chatUtility.addSpacing(parser.deserialize("<#6C719D>" + TimeUtility.formatTimeSince(mostRecentLogin)), 50, true);

            clanLines.add(tag.append(leader).append(partner).append(online).append(active).append(seen)
                    .hoverEvent(HoverEvent.showText(parser.deserialize("<#6C719D>Click to view <white>" + clan + " <#6C719D>clan roster")))
                    .clickEvent(ClickEvent.runCommand("/clan roster " + clan)));
        }

        plugin.getPaginateUtil().displayPage(args, (Player)sender, clanLines);
        return true;
    }
}
