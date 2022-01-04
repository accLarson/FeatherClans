package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;


    public ChatCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player originator = (Player) sender;
        if(args.length < 2) {
            originator.sendMessage(messages.get("clan_chat_no_message", null));
            return false;
        }

        if(!plugin.getClanManager().isOfflinePlayerInClan(originator)) {
            originator.sendMessage(messages.get("clan_chat_no_clan", null));
            return false;
        }

        String input = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        String clan = plugin.getClanManager().getClanByOfflinePlayer(originator);

        TextComponent tag =  (TextComponent) MiniMessage.builder()
                .removeDefaultTransformations()
                .transformation(TransformationType.COLOR)
                .transformation(TransformationType.RESET)
                .build()
                .parse(clan);
        TextComponent component = Component.text("[", TextColor.fromHexString(messages.getThemePrimary())).append(tag).append(Component.text("]", TextColor.fromHexString(messages.getThemePrimary())));
        TextComponent message = Component.join(Component.text(": ", TextColor.fromHexString(messages.getThemePrimary())), component, Component.text(input));

        List<OfflinePlayer> players = plugin.getClanManager().getOfflinePlayersByClan(clan);
        for (OfflinePlayer player: players) {
            if(player.isOnline()) {
                player.getPlayer().sendMessage(message);
            }
        }

        return true;
    }
}
