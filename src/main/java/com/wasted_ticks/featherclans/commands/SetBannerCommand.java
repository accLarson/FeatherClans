package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetBannerCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public SetBannerCommand(FeatherClans plugin) {
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

        if (!originator.hasPermission("feather.clans.setbanner")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);

        ItemStack mainHand = originator.getInventory().getItemInMainHand();

        // Check if the player is holding a banner
        if (mainHand.getType() == null || !mainHand.getType().name().endsWith("_BANNER")) {
            originator.sendMessage(messages.get("clan_setbanner_error_missing", Map.of(
                    "item", "banner"
            )));
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
                double amount = this.plugin.getFeatherClansConfig().getEconomySetBannerPrice();
                originator.sendMessage(messages.get("clan_economy_cost_warning", Map.of("amount", String.valueOf((int) amount))));
            }
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan setbanner")));
            return true;
        }

        boolean success = false;
        if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = this.plugin.getFeatherClansConfig().getEconomySetBannerPrice();
            if (economy.has(originator, amount)) {
                economy.withdrawPlayer(originator, amount);
                ItemStack bannerCopy = mainHand.clone();
                bannerCopy.setAmount(1);
                success = plugin.getClanManager().setBanner(tag, bannerCopy);
                if (success) {
                    originator.sendMessage(messages.get("clan_setbanner_success_economy", Map.of(
                            "amount", String.valueOf((int) amount)
                    )));
                }
            } else {
                originator.sendMessage(messages.get("clan_setbanner_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            ItemStack bannerCopy = mainHand.clone();
            bannerCopy.setAmount(1);
            success = plugin.getClanManager().setBanner(tag, bannerCopy);
        }

        if (!success) {
            originator.sendMessage(messages.get("clan_setbanner_error_generic", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_setbanner_success", null));
        return true;
    }
}
