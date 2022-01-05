package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetHomeCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public SetHomeCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.sethome")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(player);
        if (!leader) {
            player.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(player);
        Location location = player.getLocation();

        boolean success = false;
        if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = this.plugin.getFeatherClansConfig().getEconomySetHomePrice();
            if (economy.has(player, amount)) {
                economy.withdrawPlayer(player, amount);
                success = plugin.getClanManager().setClanHome(tag, location);
            } else {
                player.sendMessage(messages.get("clan_sethome_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            success = plugin.getClanManager().setClanHome(tag, location);
        }

        if (!success) {
            player.sendMessage(messages.get("clan_sethome_error_generic", null));
            return true;
        }

        player.sendMessage(messages.get("clan_sethome_success", null));
        return true;

    }
}
