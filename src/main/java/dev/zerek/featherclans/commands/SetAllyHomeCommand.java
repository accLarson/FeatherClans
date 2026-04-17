package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class SetAllyHomeCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public SetAllyHomeCommand(FeatherClans plugin) {
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

        if (!originator.hasPermission("feather.clans.setallyhome")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!plugin.getClanManager().isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (!plugin.getClanManager().hasAlly(tag)) {
            originator.sendMessage(messages.get("clan_ally_error_no_alliance", null));
            return true;
        }

        Location location = originator.getLocation();

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            if (plugin.getFeatherClansConfig().isEconomyEnabled()) {
                double amount = plugin.getFeatherClansConfig().getEconomySetHomePrice();
                originator.sendMessage(messages.get("clan_economy_cost_warning", Map.of("amount", String.valueOf((int) amount))));
            }
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan setallyhome")));
            return true;
        }

        boolean success;

        if (plugin.getFeatherClansConfig().isEconomyEnabled()) {
            Economy economy = plugin.getEconomy();
            double amount = plugin.getFeatherClansConfig().getEconomySetHomePrice();
            if (economy.has(originator, amount)) {
                economy.withdrawPlayer(originator, amount);
                success = plugin.getClanManager().setAllyHome(tag, location);
                if (success) {
                    originator.sendMessage(messages.get("clan_setallyhome_success_economy", Map.of(
                            "amount", String.valueOf((int) amount)
                    )));
                }
            } else {
                originator.sendMessage(messages.get("clan_setallyhome_error_economy", Map.of(
                        "amount", String.valueOf((int) amount)
                )));
                return true;
            }
        } else {
            success = plugin.getClanManager().setAllyHome(tag, location);
        }

        if (!success) {
            originator.sendMessage(messages.get("clan_setallyhome_error_generic", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_setallyhome_success", null));

        String allyTag = plugin.getClanManager().getAlly(tag);
        if (allyTag != null) {
            UUID allyLeaderUUID = plugin.getClanManager().getLeader(allyTag);
            Player allyLeader = allyLeaderUUID != null ? Bukkit.getPlayer(allyLeaderUUID) : null;
            if (allyLeader != null && allyLeader.isOnline()) {
                allyLeader.sendMessage(messages.get("clan_setallyhome_notification", Map.of("clan", tag)));
            }
        }

        return true;
    }
}