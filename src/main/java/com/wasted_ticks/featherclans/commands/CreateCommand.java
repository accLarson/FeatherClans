package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreateCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public CreateCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){

            Player player = (Player) sender;

            if (args.length != 2) {
                player.sendMessage(messages.get("clan_create_error_invalid_arg_length", null));
                return false;
            }

            String tag = args[1];
            if(tag.length() > 255) {
                player.sendMessage(messages.get("clan_create_error_markup_tag", null));
                return false;
            }

            if(!tag.chars().allMatch(Character::isLetter)) {
                player.sendMessage(messages.get("clan_create_error_invalid_tag", null));
                return false;
            }

            List<String> bannedTags = this.plugin.getFeatherClansConfig().getDenyTags();
            if(bannedTags.contains(tag)) {
                player.sendMessage(messages.get("clan_create_error_denied_tag", null));
                return false;
            }

            List<String> clans = plugin.getClanManager().getClans();
            if(clans.contains(tag)) {
                player.sendMessage(messages.get("clan_create_error_similar_tag", null));
                return false;
            }

            ItemStack stack = player.getInventory().getItemInMainHand();
            if (!stack.getType().name().contains("BANNER")) {
                player.sendMessage(messages.get("clan_create_error_banner", null));
                return false;
            }

            boolean inClan = plugin.getClanManager().isOfflinePlayerInClan(player);
            if(inClan) {
                player.sendMessage(messages.get("clan_create_error_in_clan", null));
                return false;
            }

            //check if has balance available to create.

            plugin.getClanManager().createClan(player, stack, tag);
            player.sendMessage(messages.get("clan_create_success", null));
        }

        return true;
    }

}
