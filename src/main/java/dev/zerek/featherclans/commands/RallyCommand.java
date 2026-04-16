package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.data.Request;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class  RallyCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    public RallyCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.rally")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player rallier = (Player) sender;

        if (!plugin.getClanManager().isOfflinePlayerInClan(rallier)) {
            rallier.sendMessage(messages.get("clan_rally_error_no_clan", null));
            return true;
        }

        if (!(plugin.getClanManager().isOfflinePlayerLeader(rallier) || plugin.getClanManager().isOfflinePlayerOfficer(rallier))) {
            rallier.sendMessage(messages.get("clan_error_leader_officer", null));
            return true;
        }

        String tag = plugin.getClanManager().getClanByOfflinePlayer(rallier);

        if (plugin.getRallyManager().isOnCooldown(tag)) {
            rallier.sendMessage(messages.get("clan_rally_error_cooldown", null));
            return true;
        }

        Location destination = rallier.getLocation();

        List<Player> targets = plugin.getClanManager().getOfflinePlayersByClan(tag).stream()
                .filter(op -> op.isOnline() && !op.getUniqueId().equals(rallier.getUniqueId()))
                .map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            rallier.sendMessage(messages.get("clan_rally_error_no_targets", null));
            return true;
        }

        for (Player target : targets) {
            plugin.getInviteManager().addRequest(Request.RequestType.RALLY, target, rallier, tag, destination);
        }

        plugin.getRallyManager().markRally(tag);

        rallier.sendMessage(messages.get("clan_rally_sent", Map.of(
                "count", String.valueOf(targets.size())
        )));

        return true;
    }
}