package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.utilities.ChatUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Sound;
import org.bukkit.OfflinePlayer;
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

public class ChatCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;


    public ChatCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.chat")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;
        if (!plugin.getClanManager().isOfflinePlayerInClan(originator)) {
            originator.sendMessage(messages.get("clan_chat_no_clan", null));
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
        String clan = plugin.getClanManager().getClanByOfflinePlayer(originator);

        List<OfflinePlayer> players = plugin.getClanManager().getOfflinePlayersByClan(clan);

        Component messageBody = ChatUtility.markHiddenMembers(plugin, message, players);
        TagResolver messageResolver = Placeholder.component("message", messageBody);

        List<Player> onlinePlayers = new ArrayList<>();
        for (OfflinePlayer player : players) {
            if (player.isOnline() && !plugin.getChatToggleManager().isChatHidden(player.getUniqueId())) {
                player.getPlayer().sendMessage(messages.get("clan_chat_message",
                        Map.of("tag", clan, "player", originator.getName()),
                        messageResolver));
                onlinePlayers.add(player.getPlayer());
            }
        }

        pingPlayers(message, onlinePlayers, originator);

        for (OfflinePlayer operator : plugin.getServer().getOperators()) {
            if (operator.isOnline()) {
                operator.getPlayer().sendMessage(messages.get("clan_chat_spy_message",
                        Map.of("tag", clan, "player", originator.getName()),
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