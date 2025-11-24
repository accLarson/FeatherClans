package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.data.Request;
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

        Request request = this.plugin.getInviteManager().getRequest(player);
        if (request == null) {
            player.sendMessage(messages.get("clan_accept_no_request", null));
            return true;
        }

        // Handle alliance acceptance
        if (request.getType() == Request.RequestType.ALLIANCE) {
            Player originator = request.getOriginator();
            String originatorClan = request.getClan();
            String acceptorClan = plugin.getClanManager().getClanByOfflinePlayer(player);

            if (acceptorClan == null) {
                player.sendMessage(messages.get("clan_error_not_in_clan", null));
                plugin.getInviteManager().clearRequest(player);
                return true;
            }

            if (!plugin.getClanManager().isOfflinePlayerLeader(player)) {
                player.sendMessage(messages.get("clan_error_leader", null));
                plugin.getInviteManager().clearRequest(player);
                return true;
            }

            if (plugin.getClanManager().hasAlly(acceptorClan)) {
                player.sendMessage(messages.get("clan_error_youre_already_allied", null));
                plugin.getInviteManager().clearRequest(player);
                return true;
            }

            if (plugin.getClanManager().hasAlly(originatorClan)) {
                player.sendMessage(messages.get("clan_error_theyre_already_allied", null));
                plugin.getInviteManager().clearRequest(player);
                return true;
            }

            // Handle economy if enabled
            if (plugin.getFeatherClansConfig().isEconomyEnabled()) {
                Economy economy = plugin.getEconomy();
                double amount = plugin.getFeatherClansConfig().getEconomyAlliancePrice();

                boolean playerHasFunds = economy.has(player, amount);
                boolean originatorHasFunds = originator != null && economy.has(originator, amount);

                if (!playerHasFunds && !originatorHasFunds) {
                    player.sendMessage(messages.get("clan_ally_accept_error_economy_both", Map.of(
                            "amount", String.valueOf((int) amount),
                            "player", originator != null ? originator.getName() : "Unknown"
                    )));
                    plugin.getInviteManager().clearRequest(player);
                    return true;
                } else if (!playerHasFunds) {
                    player.sendMessage(messages.get("clan_ally_accept_error_economy_you", Map.of(
                            "amount", String.valueOf((int) amount)
                    )));
                    plugin.getInviteManager().clearRequest(player);
                    return true;
                } else if (!originatorHasFunds) {
                    player.sendMessage(messages.get("clan_ally_accept_error_economy_other", Map.of(
                            "amount", String.valueOf((int) amount),
                            "player", originator != null ? originator.getName() : "Unknown"
                    )));
                    plugin.getInviteManager().clearRequest(player);
                    return true;
                }

                // Withdraw from both players
                economy.withdrawPlayer(player, amount);
                if (originator != null) {
                    economy.withdrawPlayer(originator, amount);
                }
            }

            // Create the alliance
            if (plugin.getClanManager().addAlliance(originatorClan, acceptorClan)) {
                player.sendMessage(messages.get("clan_ally_accept_success_player", Map.of("clan", originatorClan)));
                
                if (originator != null && originator.isOnline()) {
                    originator.sendMessage(messages.get("clan_ally_accept_success_originator", Map.of(
                            "player", player.getName(),
                            "clan", acceptorClan
                    )));
                }
                
                // Broadcast alliance to all online players
                plugin.getServer().getOnlinePlayers().forEach(p -> 
                    p.sendMessage(messages.get("clan_ally_success_broadcast", Map.of(
                            "clan1", originatorClan,
                            "clan2", acceptorClan
                    )))
                );
                
                plugin.getDisplayManager().resetDisplays();
            }

            plugin.getInviteManager().clearRequest(player);
            return true;
        }

        // Handle membership acceptance
        String tag = request.getClan();
        player.sendMessage(messages.get("clan_accept_success_player", Map.of(
                "clan", tag
        )));

        if (plugin.getFeatherClansConfig().isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = plugin.getFeatherClansConfig().getEconomyMembershipPrice();
            if (economy.has(player, amount)) {
                economy.withdrawPlayer(player, amount);
                if (plugin.getClanManager().addOfflinePlayerToClan(player, tag)) {
                    plugin.getInviteManager().clearRequest(player);
                    player.sendMessage(messages.get("clan_accept_success_player", Map.of(
                            "clan", tag
                    )));
                }
            } else {
                player.sendMessage(messages.get("clan_accept_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            if (plugin.getClanManager().addOfflinePlayerToClan(player, tag)) {
                plugin.getInviteManager().clearRequest(player);
                player.sendMessage(messages.get("clan_accept_success_player", Map.of(
                        "clan", tag
                )));
            }
        }

        return true;
    }
}
