package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.utilities.ChatUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllyChatCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public AllyChatCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.allychat")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerInClan(originator)) {
            originator.sendMessage(messages.get("clan_allychat_no_clan", null));
            return true;
        }

        String clan = plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (!plugin.getClanManager().hasAlly(clan)) {
            originator.sendMessage(messages.get("clan_allychat_no_ally", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_chat_no_message", null));
            return true;
        }

        boolean wasHidden = plugin.getChatToggleManager().isChatHidden(originator.getUniqueId());
        plugin.getChatToggleManager().setChatHidden(originator.getUniqueId(), false);
        if (wasHidden) {
            originator.sendMessage(messages.get("clan_showchat", null));
        }

        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        String allyClan = plugin.getClanManager().getAlly(clan.toLowerCase());

        List<OfflinePlayer> clanPlayers = plugin.getClanManager().getOfflinePlayersByClan(clan);
        List<OfflinePlayer> allyPlayers = plugin.getClanManager().getOfflinePlayersByClan(allyClan);

        List<OfflinePlayer> candidates = new ArrayList<>(clanPlayers);
        candidates.addAll(allyPlayers);
        Component messageBody = ChatUtility.markHiddenMembers(plugin, message, candidates);
        TagResolver messageResolver = Placeholder.component("message", messageBody);

        List<Player> allRecipients = new ArrayList<>();

        // Send to own clan members
        for (OfflinePlayer player : clanPlayers) {
            if (player.isOnline() && !plugin.getChatToggleManager().isChatHidden(player.getUniqueId())) {
                player.getPlayer().sendMessage(messages.get("clan_allychat_message",
                        Map.of("tag", clan, "player", originator.getName()),
                        messageResolver));
                allRecipients.add(player.getPlayer());
            }
        }

        // Send to ally clan members
        for (OfflinePlayer player : allyPlayers) {
            if (player.isOnline() && !plugin.getChatToggleManager().isChatHidden(player.getUniqueId())) {
                player.getPlayer().sendMessage(messages.get("clan_allychat_message",
                        Map.of("tag", clan, "player", originator.getName()),
                        messageResolver));
                allRecipients.add(player.getPlayer());
            }
        }

        pingPlayers(message, allRecipients, originator);

        // Send to operators for spy
        for (OfflinePlayer operator : plugin.getServer().getOperators()) {
            if (operator.isOnline()) {
                operator.getPlayer().sendMessage(messages.get("clan_allychat_spy_message",
                        Map.of("tag", clan, "ally", allyClan, "player", originator.getName()),
                        messageResolver));
            }
        }

        return true;
    }

    private void pingPlayers(String message, List<Player> recipients, Player sender) {
        Sound sound = plugin.getFeatherClansConfig().getPingSound();
        float volume = plugin.getFeatherClansConfig().getPingVolume();
        float pitch = plugin.getFeatherClansConfig().getPingPitch();

        // Strip punctuation from message
        List<String> words = Arrays.asList(message.toLowerCase().split("[^a-z0-9_]+"));

        // Filter recipients whose names appear in the word list
        List<Player> playersToPing = recipients.stream()
                .filter(player -> words.contains(player.getName().toLowerCase()))
                .collect(Collectors.toList());

        // Play sound for each matched player
        playersToPing.forEach(p -> p.playSound(p.getLocation(), sound, volume, pitch));
    }
}