package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.ColoredTagUtility;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetTagCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public SetTagCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.settag")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_settag_guide", null));
            originator.sendMessage(messages.get("clan_settag_colors", Map.of("colors", ColoredTagUtility.getColorOptions())));
            return true;
        }

        if (tag.equalsIgnoreCase(args[1])) {
            originator.sendMessage(messages.get("clan_settag_guide", null));
            originator.sendMessage(messages.get("clan_settag_colors", Map.of("colors", ColoredTagUtility.getColorOptions())));
            return true;        }

        if (!ColoredTagUtility.isValidColoredTag(args[1], tag)) {
            originator.sendMessage(messages.get("clan_settag_error_invalid_input", Map.of("tag", tag)));
            originator.sendMessage(messages.get("clan_settag_guide", null)); 
            originator.sendMessage(messages.get("clan_settag_colors", Map.of("colors", ColoredTagUtility.getColorOptions())));

            return true;
        }

        String coloredTag = ColoredTagUtility.convert(args[1]);

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_settag_draft", null).append(MiniMessage.miniMessage().deserialize("<white>" + coloredTag)));
            if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
                double amount = this.plugin.getFeatherClansConfig().getEconomySetTagPrice();
                originator.sendMessage(messages.get("clan_economy_cost_warning", Map.of("amount", String.valueOf((int) amount))));
            }
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan settag " + args[1])));
            return true;
        }

        boolean success = false;
        if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = this.plugin.getFeatherClansConfig().getEconomySetTagPrice();
            if (economy.has(originator, amount)) {
                economy.withdrawPlayer(originator, amount);
                success = plugin.getClanManager().setColorTag(tag, coloredTag);
                if (success) {
                    originator.sendMessage(messages.get("clan_settag_success_economy", Map.of(
                            "amount", String.valueOf((int) amount)
                    )));
                    this.plugin.getDisplayManager().resetDisplays();
                }
            } else {
                originator.sendMessage(messages.get("clan_settag_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            success = plugin.getClanManager().setColorTag(tag, coloredTag);
        }

        if (!success) {
            originator.sendMessage(messages.get("clan_settag_error_generic", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_settag_success", null));
        return true;
    }
}
