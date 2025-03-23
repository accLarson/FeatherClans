package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ManageCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ManageCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    // clan manage test chat    <message>

    // clan manage test confer  <clan-member>
    // clan manage test kick    <clan-member>
    // clan manage test invite  <player>


    // clan manage test disband
    // clan manage test sethome

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        ClanManager manager = plugin.getClanManager();

        if (!sender.hasPermission("feather.clans.manage") || sender instanceof ConsoleCommandSender) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(messages.get("clan_manage_error_args",null));
            return true;
        }

        String tag = args[1];

        if (manager.getClans().stream().noneMatch(tag::equalsIgnoreCase)) {
            sender.sendMessage(messages.get("clan_manage_error_unresolved_clan", null));
            return true;
        }

        switch (args[2]) {

            case "chat":

                if (args.length < 4) {
                    sender.sendMessage(messages.get("clan_chat_no_message", null));
                    return true;
                }

                String message = Arrays.stream(args).skip(3).collect(Collectors.joining(" "));
                
                for (OfflinePlayer player : manager.getOfflinePlayersByClan(tag)) {
                    if (player.isOnline()) {
                        player.getPlayer().sendMessage(messages.get("clan_chat_message", Map.of(
                                "tag", tag,
                                "player", sender.getName(),
                                "message", message
                        )));
                    }
                }
                for (OfflinePlayer operator : plugin.getServer().getOperators()) {
                    if (operator.isOnline() && sender != operator) {
                        operator.getPlayer().sendMessage(messages.get("clan_chat_spy_message", Map.of(
                                "tag", tag,
                                "player", sender.getName(),
                                "message", message
                        )));
                    }
                }
                break;

            case "confer":

                if (args.length != 4) {
                    sender.sendMessage(messages.get("clan_confer_no_player", null));
                    break;
                }
                OfflinePlayer potentialLeader = Bukkit.getOfflinePlayer(args[3]);

                if (manager.isOfflinePlayerInSpecificClan(potentialLeader, tag)) {
                    sender.sendMessage(messages.get("clan_manage_confer_not_in_clan", null));
                    break;
                }

                if (manager.isOfflinePlayerLeader(potentialLeader)) {
                    sender.sendMessage(messages.get("clan_manage_confer_error_leader", null));
                    break;
                }

                if (manager.setClanLeader(tag, potentialLeader)) {
                    sender.sendMessage(messages.get("clan_manage_confer_success_originator", Map.of(
                            "player", potentialLeader.getName(),
                            "clan", tag
                    )));
                    if (potentialLeader.isOnline()) {
                        ((Player)potentialLeader).sendMessage(messages.get("clan_manage_confer_success_player", Map.of(
                                "player", sender.getName(),
                                "clan", tag
                        )));
                    }
                } else sender.sendMessage(messages.get("clan_confer_error_generic", null));
                break;

            case "invite":

                if (!(sender instanceof Player)) {
                    sender.sendMessage(messages.get("clan_error_player", null));
                    break;
                }

                if (args.length != 4) {
                    sender.sendMessage(messages.get("clan_invite_error_no_player_specified", null));
                    break;
                }

                Player invitee = Bukkit.getPlayer(args[3]);

                if (invitee == null) {
                    sender.sendMessage(messages.get("clan_invite_error_unresolved_player", null));
                    break;
                }
                
                if (manager.isOfflinePlayerInClan(invitee)) {
                    sender.sendMessage(messages.get("clan_invite_error_already_in_clan", null));
                    break;
                }
                
                int max = this.plugin.getFeatherClansConfig().getClanMaxMembers();
                
                if (manager.getOfflinePlayersByClan(tag).size() >= max) {
                    sender.sendMessage(messages.get("clan_manage_invite_error_max", Map.of(
                            "max", String.valueOf(max)
                    )));
                    break;
                }

                plugin.getInviteManager().invite(invitee, tag, (Player) sender);
                break;

            case "kick":

                if (args.length != 4) {
                    sender.sendMessage(messages.get("clan_kick_error_no_player_specified", null));
                    break;
                }
                
                OfflinePlayer kickee = Bukkit.getOfflinePlayer(args[3]);

                if (!kickee.hasPlayedBefore()) {
                    sender.sendMessage(messages.get("clan_kick_error_unresolved_player", null));
                    break;
                }

                if (manager.isOfflinePlayerInSpecificClan(kickee, tag)) {
                    sender.sendMessage(messages.get("clan_manage_kick_error_not_in_clan", Map.of(
                            "clan", tag
                    )));
                    break;
                }

                if (manager.isOfflinePlayerLeader(kickee)) {
                    sender.sendMessage(messages.get("clan_manage_kick_error_leader", null));
                    break;
                }

                if (!manager.resignOfflinePlayer(kickee)) {
                    sender.sendMessage(messages.get("clan_kick_error", null));
                    break;
                }

                sender.sendMessage(messages.get("clan_kick_success", Map.of(
                        "player", kickee.getName()
                )));

                if (kickee.isOnline()){
                    kickee.getPlayer().sendMessage(messages.get("clan_kick_success_target", Map.of(
                            "clan", tag
                    )));
                }
                plugin.getActiveManager().updateActiveStatus(kickee, tag);

                break;

            case "disband":

                for (OfflinePlayer member : manager.getOfflinePlayersByClan(tag)) {
                    manager.resignOfflinePlayer(member);
                }
                if (manager.deleteClan(tag)) {
                    sender.sendMessage(messages.get("clan_disband_success", Map.of("clan", tag)));
                    plugin.getServer()
                            .getOnlinePlayers()
                            .forEach(p -> p.sendMessage(messages.get("clan_disband_broadcast", Map.of("clan", tag.toLowerCase()))));

                    plugin.getActiveManager().removeClan(tag.toLowerCase());
                }
                else sender.sendMessage(messages.get("clan_disband_error_generic", null));
                break;

            case "sethome":

                if (!(sender instanceof Player)) {
                    sender.sendMessage(messages.get("clan_error_player", null));
                    break;
                }

                Player player = (Player) sender;

                if (plugin.getClanManager().setClanHome(tag, player.getLocation())) player.sendMessage(messages.get("clan_sethome_success",Map.of(
                        "clan", tag
                )));

                else player.sendMessage(messages.get("clan_sethome_error_generic",null));

        }
        this.plugin.getDisplayManager().resetDisplays();
        return true;
    }
}
