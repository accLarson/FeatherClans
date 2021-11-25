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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ListCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player)){
            sender.sendMessage(messages.get("clan_error_player"));
            return false;
        }

        Player player = (Player) sender;
        List<Clan> clans = plugin.getClanManager().getClans();
        if(clans.isEmpty()) {
            player.sendMessage(messages.get("clan_list_no_clans"));
            return false;
        }

        ChatUtil chatUtil = new ChatUtil(this.plugin);
        MiniMessage parser = MiniMessage.builder()
                .removeDefaultTransformations()
                .transformation(TransformationType.COLOR)
                .transformation(TransformationType.RESET)
                .build();

        player.sendMessage(messages.get("clan_pre_line"));
        player.sendMessage("");
        TextComponent total = Component.text("").append(messages.get("clan_list_total")).append(Component.text(clans.size(), TextColor.fromHexString(messages.getThemePrimary())));
        player.sendMessage(total);
        player.sendMessage("");
        TextComponent divider = Component.text("|", TextColor.fromHexString(messages.getThemePrimary()));
        for (Clan clan: clans) {
            TextComponent tag = chatUtil.addSpacing((TextComponent)parser.parse(clan.getString("tag")), 50);
            TextComponent size = chatUtil.addSpacing(Component.text(plugin.getClanManager().getOfflinePlayersByClan(clan).size()), 20, true);

            player.sendMessage(Component.join(divider, tag, size.color(TextColor.fromHexString(messages.getThemePrimary()))));
        }
        player.sendMessage(messages.get("clan_line"));

        return true;
    }
}
