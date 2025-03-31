package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class OfficerCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final ClanManager manager;
    private final FeatherClansMessages messages;


    public OfficerCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.manager = plugin.getClanManager();
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.officer")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!manager.isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length < 2) {
            originator.sendMessage(messages.get("clan_officer_error_usage", null));
            return true;
        }

        if (!(args[1].equalsIgnoreCase("promote") || args[1].equalsIgnoreCase("demote"))) {
            originator.sendMessage(messages.get("clan_officer_error_usage", null));
            return true;
        }

        boolean status = args[1].equalsIgnoreCase("promote");

        if (args.length < 3) {
            originator.sendMessage(messages.get("clan_officer_no_player", null));
            return true;
        }

        OfflinePlayer officer = Bukkit.getOfflinePlayer(args[2]);

        String clan = this.plugin.getClanManager().getClanByOfflinePlayer(originator);

        if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(officer, clan)) {

            if (this.plugin.getClanManager().isUsernameInSpecificClan(args[2],clan)) {
                UUID uuid = this.plugin.getClanManager().getUUIDFromUsername(args[2]);
                officer = Bukkit.getOfflinePlayer(uuid);
            }
            else {
                originator.sendMessage(messages.get("clan_kick_error_not_in_clan", null));
                return true;
            }
        }


        if (this.manager.isOfflinePlayerLeader(officer)) {
            originator.sendMessage(messages.get("clan_officer_error_leader", null));
            return true;
        }

        if (status && this.manager.isOfflinePlayerOfficer(officer)) {
            originator.sendMessage(messages.get("clan_officer_error_already_officer", null));
            return true;
        }

        if (!status && !this.manager.isOfflinePlayerOfficer(officer)) {
            originator.sendMessage(messages.get("clan_officer_error_not_officer", null));
            return true;
        }

        if (args.length < 4 || !args[3].equalsIgnoreCase("confirm")) {
            originator.sendMessage(messages.get("clan_command_confirm", Map.of("command", "/clan officer " + officer.getName())));
            return true;
        }

        boolean successful = manager.setClanOfficerStatus(officer, status);

        if (successful) {
            if (status) {
                originator.sendMessage(messages.get("clan_officer_promote_success_originator", Map.of("player", officer.getName())));
                if (officer.isOnline()) {((Player)officer).sendMessage(messages.get("clan_officer_promote_success_player", Map.of("player", originator.getName(), "clan", clan)));}
            }
            else {
                originator.sendMessage(messages.get("clan_officer_demote_success_originator", Map.of("player", officer.getName())));
                if (officer.isOnline()) {((Player)officer).sendMessage(messages.get("clan_officer_demote_success_player", Map.of("player", originator.getName(), "clan", clan)));}

            }
        } else {
            originator.sendMessage(messages.get("clan_officer_error_generic", null));
        }
        return true;
    }
}
