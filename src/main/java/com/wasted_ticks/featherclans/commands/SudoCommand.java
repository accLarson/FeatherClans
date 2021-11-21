package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class SudoCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public SudoCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("SudoCommand");

        //everything below is currently just used for testing.

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(messages.get("clan_create_error_invalid_arg_length"));
            return false;
        }

        // tag = <blue>TE<bold>ST
        String tag = args[1];

        TextComponent component =  (TextComponent) MiniMessage.builder()
                .removeDefaultTransformations()
                .transformation(TransformationType.COLOR)
                .transformation(TransformationType.RESET)
                .build()
                .parse(tag);

        // coloured = &9TE<bold>ST
        String coloured = LegacyComponentSerializer.legacySection().serialize(component);

        String idk = GsonComponentSerializer.gson().serialize(component);


        // plain = TE<bold>ST
        String plain = PlainTextComponentSerializer.plainText().serialize(component);

        player.sendMessage("component serialized (json):" + idk);
        player.sendMessage("component serialized (legacy):" + coloured);
        player.sendMessage("component serialized (plain): " + plain);

        // false due to presence of <bold>.
        player.sendMessage("all-match character isLetter (stripped): " + plain.chars().allMatch(Character::isLetter));

        return false;
    }
}
