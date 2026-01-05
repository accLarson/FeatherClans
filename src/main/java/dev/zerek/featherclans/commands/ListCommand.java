package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import java.util.UUID;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.utilities.ChatUtility;
import dev.zerek.featherclans.utilities.TimeUtility;
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
        for (MetadataValue meta : player.getMetadata("vanished"))
            if (meta.asBoolean()) return true;
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

        // Pre-compute alt status and last login times for all players to avoid repeated blocking calls during sorting
        Map<UUID, Boolean> altCache = new HashMap<>();
        Map<UUID, Long> lastLoginCache = new HashMap<>();
        
        for (String clan : clans) {
            List<OfflinePlayer> members = plugin.getClanManager().getOfflinePlayersByClan(clan);
            for (OfflinePlayer member : members) {
                if (!altCache.containsKey(member.getUniqueId())) {
                    altCache.put(member.getUniqueId(), plugin.getAltUtility().isAlt(member));
                    lastLoginCache.put(member.getUniqueId(), member.getLastLogin());
                }
            }
        }

        // Sort clans by active member count first, then online count, then most recent login time, then total member count
        List<String> sortedClans = clans.stream()
                .sorted(
                    Comparator
                        // First by active count
                        .comparingInt((String clan) -> plugin.getActiveManager().getActiveMemberCount(clan))
                        // Then by online count
                        .thenComparingInt(clan -> {
                            List<OfflinePlayer> members = plugin.getClanManager().getOfflinePlayersByClan(clan);
                            return (int) members.stream()
                                .filter(member -> !altCache.getOrDefault(member.getUniqueId(), false))
                                .filter(member -> member.isOnline() && !this.isVanished(member.getPlayer()))
                                .count();
                        })
                        // Finally by clan size
                        .thenComparingInt(clan -> {
                            List<OfflinePlayer> members = plugin.getClanManager().getOfflinePlayersByClan(clan);
                            return (int) members.stream()
                                .filter(member -> !altCache.getOrDefault(member.getUniqueId(), false))
                                .count();
                        })
                        .reversed()
                )
                .collect(Collectors.toList());

        ChatUtility chatUtility = new ChatUtility(this.plugin);
        MiniMessage parser = MiniMessage.builder().tags(TagResolver.builder().resolver(StandardTags.color()).resolver(StandardTags.reset()).build()).build();

        List<Component> clanLines = new ArrayList<>();

        // Active header hoverable text
        Component activeHeader = parser.deserialize("<gray>Active");
        int memberReq = plugin.getFeatherClansConfig().getClanActiveMembersRequirement();
        int dayReq = plugin.getFeatherClansConfig().getClanInactiveDaysThreshold();
        String hoverText = "<#7FD47F>Active clan status <#6C719D>requirement:\n" + "<white>" + memberReq + "+ <#6C719D>members seen within <white>" + dayReq + " <#6C719D>days";
        activeHeader = activeHeader.hoverEvent(HoverEvent.showText(parser.deserialize(hoverText)));

        Component header = chatUtility.addSpacing(parser.deserialize("<gray>Clan"), 45)
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Leader"), 102))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Ally"), 42))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Online"), 44, true))
                .append(chatUtility.addSpacing(activeHeader, 44, true))
                .append(chatUtility.addSpacing(parser.deserialize("<gray>Seen"), 40, true));

        clanLines.add(header);
        clanLines.add(messages.get("clan_line", null));

        for (String clan : sortedClans) {
            String coloredTag = null;
            if (plugin.getActiveManager().isActive(clan)) coloredTag = plugin.getClanManager().getColorTag(clan);
            String formattedTag = (coloredTag == null) ? clan : coloredTag;


            List<OfflinePlayer> clanMembers = plugin.getClanManager().getOfflinePlayersByClan(clan);
            
            // Count members excluding alts
            long onlineNonAltCount = clanMembers.stream()
                .filter(member -> !altCache.getOrDefault(member.getUniqueId(), false))
                .filter(member -> member.isOnline() && !this.isVanished(member.getPlayer()))
                .count();
            
            long totalNonAltCount = clanMembers.stream()
                .filter(member -> !altCache.getOrDefault(member.getUniqueId(), false))
                .count();
            
            // Get the most recent login time from clan members
            long mostRecentLogin = clanMembers.stream()
                .mapToLong(member -> lastLoginCache.getOrDefault(member.getUniqueId(), 0L))
                .max()
                .orElse(0);

            // Use ActiveManager to get active member count
            String activeCount = String.valueOf(plugin.getActiveManager().getActiveMemberCount(clan));

            // Check if clan is active and use pastel green color if it is
            String activeValueColor = "<gray>";
            if (activeCount.equals("0")) activeValueColor = "<dark_gray>";
            else if (plugin.getActiveManager().isActive(clan)) activeValueColor = "<#7FD47F>"; // Pastel green color

            Component activeComponent = parser.deserialize(activeValueColor + activeCount);

            // variable for ally
            String allyTag = plugin.getClanManager().hasAlly(clan) ? plugin.getClanManager().getAlly(clan.toLowerCase()) : null;
            String coloredAlly = (allyTag != null && plugin.getActiveManager().isActive(allyTag)) ? plugin.getClanManager().getColorTag(allyTag) : null;
            String allyText = (allyTag != null) ? ((coloredAlly == null) ? "<#949bd1>" + allyTag : coloredAlly) : "<#949bd1>-";

            Component tag = chatUtility.addSpacing(parser.deserialize(formattedTag), 45);
            Component leader = chatUtility.addSpacing(parser.deserialize("<#949bd1>" + Bukkit.getOfflinePlayer(plugin.getClanManager().getLeader(clan)).getName()), 102);

            Component ally = chatUtility.addSpacing(parser.deserialize(allyText), 42);
            Component online = chatUtility.addSpacing(parser.deserialize("<#6C719D>" + onlineNonAltCount + "/" + totalNonAltCount), 44, true);
            Component active = chatUtility.addSpacing(activeComponent, 44, true);
            Component seen = chatUtility.addSpacing(parser.deserialize("<#6C719D>" + TimeUtility.formatTimeSince(mostRecentLogin)), 40, true);

            clanLines.add(tag.append(leader).append(ally).append(online).append(active).append(seen)
                    .hoverEvent(HoverEvent.showText(parser.deserialize("<#6C719D>Click to view <white>" + clan + " <#6C719D>clan roster")))
                    .clickEvent(ClickEvent.runCommand("/clan roster " + clan)));
        }

        plugin.getPaginateUtil().displayPage(args, (Player)sender, clanLines);
        return true;
    }
}
