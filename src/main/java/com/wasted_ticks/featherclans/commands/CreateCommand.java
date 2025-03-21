package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final FeatherClansConfig config;

    public CreateCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.config = plugin.getFeatherClansConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.create")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_create_error_invalid_arg_length", null));
            return true;
        }

        String tag = args[1];

        int max = config.getClanMaxTagSize();

        if (tag.length() > max) {
            originator.sendMessage(messages.get("clan_create_error_length_tag_max", Map.of(
                    "length", max + ""
            )));
            return true;
        }

        int min = config.getClanMinTagSize();

        if (tag.length() < min) {
            originator.sendMessage(messages.get("clan_create_error_length_tag_min", Map.of(
                    "length", min + ""
            )));
            return true;
        }

        if (!tag.chars().allMatch(Character::isLetter)) {
            originator.sendMessage(messages.get("clan_create_error_invalid_tag", null));
            return true;
        }

        List<String> bannedTags = this.plugin.getFeatherClansConfig().getDenyTags();

        if (bannedTags.contains(tag)) {
            originator.sendMessage(messages.get("clan_create_error_denied_tag", null));
            return true;
        }

        List<String> clans = plugin.getClanManager().getClans();

        if (clans.contains(tag.toLowerCase())) {
            originator.sendMessage(messages.get("clan_create_error_similar_tag", null));
            return true;
        }

        ItemStack stack = originator.getInventory().getItemInMainHand();

        if (!stack.getType().name().contains("BANNER")) {
            originator.sendMessage(messages.get("clan_create_error_banner", null));
            return true;
        }

        boolean inClan = plugin.getClanManager().isOfflinePlayerInClan(originator);

        if (inClan) {
            originator.sendMessage(messages.get("clan_create_error_in_clan", null));
            return true;
        }

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan create " + tag)));
            return true;
        }

        boolean created;

        if (config.isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = config.getEconomyCreationPrice();
            if (economy.has(originator, amount)) {
                economy.withdrawPlayer(originator, amount);
                created = plugin.getClanManager().createClan(originator, stack, tag.toLowerCase());
                if(created) {
                    originator.sendMessage(messages.get("clan_create_success_economy", Map.of(
                            "amount", String.valueOf((int) amount)
                    )));
                }

            } else {
                originator.sendMessage(messages.get("clan_create_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            created = plugin.getClanManager().createClan(originator, stack, tag.toLowerCase());
        }

        if(!created) {
            originator.sendMessage(messages.get("clan_create_error_generic", null));
        } else {
            plugin.getServer()
                .getOnlinePlayers()
                .forEach(p -> p.sendMessage(messages.get("clan_create_success", Map.of("clan", tag.toLowerCase()))));
        }
        return true;
    }

}
