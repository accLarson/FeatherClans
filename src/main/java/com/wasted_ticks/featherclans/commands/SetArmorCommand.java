package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetArmorCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public SetArmorCommand(FeatherClans plugin) {
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

        if (!originator.hasPermission("feather.clans.setarmor")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);
        ItemStack chestplate = originator.getInventory().getChestplate();
        ItemStack leggings = originator.getInventory().getLeggings();
        ItemStack boots = originator.getInventory().getBoots();

        if (chestplate == null || leggings == null || boots == null) {
            originator.sendMessage(messages.get("clan_setarmor_error_missing", null));
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
                double amount = this.plugin.getFeatherClansConfig().getEconomySetArmorPrice();
                originator.sendMessage(messages.get("clan_economy_cost_warning", Map.of("amount", String.valueOf((int) amount))));
            }
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan setarmor")));
            return true;
        }

        boolean success = false;
        if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = this.plugin.getFeatherClansConfig().getEconomySetArmorPrice();
            if (economy.has(originator, amount)) {
                economy.withdrawPlayer(originator, amount);
                success = plugin.getClanManager().setClanArmor(tag, chestplate, leggings, boots);
                if(success) {
                    originator.sendMessage(messages.get("clan_setarmor_success_economy", Map.of(
                            "amount", String.valueOf((int) amount)
                    )));
                    this.plugin.getDisplayManager().resetDisplays();
                }
            } else {
                originator.sendMessage(messages.get("clan_setarmor_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            success = plugin.getClanManager().setClanArmor(tag, chestplate, leggings, boots);
        }

        if (!success) {
            originator.sendMessage(messages.get("clan_setarmor_error_generic", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_setarmor_success", null));
        return true;
    }
}
