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

import java.util.List;
import java.util.Map;

public class ElectCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final ClanManager clanManager;

    public ElectCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player originator = (Player) sender;

        if (!originator.hasPermission("feather.clans.appoint")) {
            originator.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!clanManager.isOfflinePlayerInClan(originator)) {
            originator.sendMessage(messages.get("clan_elect_no_clan", null));
            return true;
        }

        String clan = clanManager.getClanByOfflinePlayer(originator);

        if (clanManager.isOfflinePlayerLeader(originator)) {
            originator.sendMessage(messages.get("clan_elect_error_is_leader", null));
            return true;
        }

        OfflinePlayer currentLeader = Bukkit.getOfflinePlayer(clanManager.getLeader(clan));

        if (!plugin.getActivityUtil().isInactive(currentLeader)) {
            originator.sendMessage(messages.get("clan_elect_active_leader", null));
            return true;
        }

        List<OfflinePlayer> clanOfficers = clanManager.getClanOfficers(clan);

        for (OfflinePlayer officer : clanOfficers) {
            if (!plugin.getActivityUtil().isInactive(officer)) {
                originator.sendMessage(messages.get("clan_elect_active_officer", null));
                return true;
            }
        }
        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_elect_no_player", null));
            return true;
        }

        Player potentialOfficer = Bukkit.getPlayer(args[1]);

        if (potentialOfficer == null) {
            originator.sendMessage(messages.get("clan_elect_unresolved_player", null));
            return true;
        }

        if (originator == potentialOfficer) {
            originator.sendMessage(messages.get("clan_elect_error_self", null));
            return true;
        }

        if (clanManager.isOfflinePlayerInSpecificClan(potentialOfficer, clan)) {
            originator.sendMessage(messages.get("clan_elect_not_in_clan", null));
            return true;
        }
        
        if (!potentialOfficer.isOnline()) {
            originator.sendMessage(messages.get("clan_elect_offline_player", null));
            return true;
        }

        clanManager.setClanLeader(clan, potentialOfficer);
        clanManager.resignOfflinePlayer(currentLeader);

        plugin.getLogger().info(potentialOfficer.getName() + " was appointed leader of " + clan + " clan by clan member " + originator.getName() + ".");
        plugin.getLogger().info(currentLeader.getName() + ",the previous leader of " + clan + ", was kicked from the clan automatically.");

        originator.sendMessage(messages.get("clan_elect_success_originator", Map.of(
                "player", potentialOfficer.getName()
        )));
        potentialOfficer.sendMessage(messages.get("clan_elect_success_player", Map.of(
                "player", originator.getName(),
                "clan", clan
        )));

        return true;
    }
}
