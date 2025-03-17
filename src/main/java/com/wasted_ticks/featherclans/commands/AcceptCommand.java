package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.utilities.RequestUtility;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AcceptCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public AcceptCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.accept")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;
        boolean inClan = plugin.getClanManager().isOfflinePlayerLeader(player);
        if (inClan) {
            player.sendMessage(messages.get("clan_accept_in_clan", null));
            return false;
        }

        RequestUtility request = this.plugin.getInviteManager().getRequest(player);
        if (request == null) {
            player.sendMessage(messages.get("clan_accept_no_request", null));
            return false;
        }

        String tag = request.getClan();
        Player originator = request.getOriginator();

        boolean success = false;
        if (this.plugin.getFeatherClansConfig().isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = this.plugin.getFeatherClansConfig().getEconomyInvitePrice();
            if (economy.has(player, amount)) {
                economy.withdrawPlayer(player, amount);
                success = plugin.getClanManager().addOfflinePlayerToClan(player, tag);
            } else {
                player.sendMessage(messages.get("clan_accept_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            success = plugin.getClanManager().addOfflinePlayerToClan(player, tag);

        }

        if(success) {
            plugin.getInviteManager().clearRequest(player);
            player.sendMessage(messages.get("clan_accept_success_player", Map.of(
                    "clan", tag
            )));
            originator.sendMessage(messages.get("clan_accept_success_originator", Map.of(
                    "player", player.getName()
            )));

            plugin.getActiveManager().updateActiveStatus(player, tag);
        }
        return true;
    }
}
