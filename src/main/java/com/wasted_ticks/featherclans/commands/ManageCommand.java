package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Request;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ManageCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;
    private final FeatherClansConfig config;

    public ManageCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
        this.config = plugin.getFeatherClansConfig();
    }

    // clan manage <tag> chat    <message>
    // clan manage <tag> confer  <clan-member>
    // clan manage <tag> kick    <clan-member>
    // clan manage <tag> invite  <player>
    // clan manage <tag> officer [promote|demote] <clan-member>
    // clan manage <tag> setarmor
    // clan manage <tag> setbanner
    // clan manage <tag> settag <colored-tag>
    // clan manage <tag> ally [propose|dissolve] [target-tag]
    // clan manage <tag> disband
    // clan manage <tag> sethome

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        ClanManager manager = plugin.getClanManager();

        if (!sender.hasPermission("feather.clans.manage") && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(messages.get("clan_manage_error_args", null));
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
                OfflinePlayer currentLeader = Bukkit.getOfflinePlayer(manager.getLeader(tag));

                if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(potentialLeader, tag)) {

                    if (this.plugin.getClanManager().isUsernameInSpecificClan(args[3], tag)) {
                        UUID uuid = this.plugin.getClanManager().getUUIDFromUsername(args[3]);
                        potentialLeader = Bukkit.getOfflinePlayer(uuid);
                    } else {
                        sender.sendMessage(messages.get("clan_kick_error_not_in_clan", null));
                        return true;
                    }
                }

                boolean successful = manager.setClanLeader(tag, potentialLeader);

                if (successful) {
                    this.plugin.getClanManager().setClanOfficerStatus(potentialLeader, false);
                    this.plugin.getClanManager().setClanOfficerStatus(currentLeader, true);

                    sender.sendMessage(messages.get("clan_confer_success_originator", Map.of(
                            "player", potentialLeader.getName()
                    )));
                    if (potentialLeader.isOnline()) {
                        ((Player) potentialLeader).sendMessage(messages.get("clan_confer_success_player", Map.of(
                                "player", sender.getName(),
                                "clan", tag
                        )));
                    }
                    this.plugin.getDisplayManager().resetDisplays();
                } else {
                    sender.sendMessage(messages.get("clan_confer_error_generic", null));
                }
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

                plugin.getInviteManager().addRequest(Request.RequestType.MEMBERSHIP, invitee, (Player) sender, tag);
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

                if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(kickee, tag)) {

                    if (this.plugin.getClanManager().isUsernameInSpecificClan(args[3], tag)) {
                        UUID uuid = this.plugin.getClanManager().getUUIDFromUsername(args[1]);
                        kickee = Bukkit.getOfflinePlayer(uuid);
                    } else {
                        sender.sendMessage(messages.get("clan_kick_error_not_in_clan", null));
                        return true;
                    }
                }

                if (manager.isOfflinePlayerLeader(kickee)) {
                    sender.sendMessage(messages.get("clan_manage_kick_error_leader", null));
                    break;
                }

                if (this.plugin.getClanManager().isOfflinePlayerOfficer(kickee)) {
                    sender.sendMessage(messages.get("clan_kick_error_officer", null));
                    break;
                }

                if (!manager.resignOfflinePlayer(kickee)) {
                    sender.sendMessage(messages.get("clan_kick_error", null));
                    break;
                }

                sender.sendMessage(messages.get("clan_kick_success", Map.of(
                        "player", kickee.getName()
                )));

                if (kickee.isOnline()) {
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
                } else sender.sendMessage(messages.get("clan_disband_error_generic", null));
                break;

            case "sethome":

                if (!(sender instanceof Player)) {
                    sender.sendMessage(messages.get("clan_error_player", null));
                    break;
                }

                Player player = (Player) sender;

                if (plugin.getClanManager().setClanHome(tag, player.getLocation()))
                    player.sendMessage(messages.get("clan_sethome_success", Map.of(
                            "clan", tag
                    )));

                else player.sendMessage(messages.get("clan_sethome_error_generic", null));
                break;

            case "officer":

                if (args.length < 4) {
                    sender.sendMessage(messages.get("clan_officer_error_usage", null));
                    return true;
                }

                if (!(args[3].equalsIgnoreCase("promote") || args[3].equalsIgnoreCase("demote"))) {
                    sender.sendMessage(messages.get("clan_officer_error_usage", null));
                    return true;
                }

                boolean officerStatus = args[3].equalsIgnoreCase("promote");

                if (args.length < 5) {
                    sender.sendMessage(messages.get("clan_officer_no_player", null));
                    return true;
                }

                OfflinePlayer officerTarget = Bukkit.getOfflinePlayer(args[4]);

                if (!this.plugin.getClanManager().isOfflinePlayerInSpecificClan(officerTarget, tag)) {
                    if (this.plugin.getClanManager().isUsernameInSpecificClan(args[4], tag)) {
                        UUID uuid = this.plugin.getClanManager().getUUIDFromUsername(args[4]);
                        officerTarget = Bukkit.getOfflinePlayer(uuid);
                    } else {
                        sender.sendMessage(messages.get("clan_kick_error_not_in_clan", null));
                        return true;
                    }
                }

                if (manager.isOfflinePlayerLeader(officerTarget)) {
                    sender.sendMessage(messages.get("clan_officer_error_leader", null));
                    return true;
                }

                if (officerStatus && manager.isOfflinePlayerOfficer(officerTarget)) {
                    sender.sendMessage(messages.get("clan_officer_error_already_officer", null));
                    return true;
                }

                if (!officerStatus && !manager.isOfflinePlayerOfficer(officerTarget)) {
                    sender.sendMessage(messages.get("clan_officer_error_not_officer", null));
                    return true;
                }

                boolean officerSuccess = manager.setClanOfficerStatus(officerTarget, officerStatus);

                if (officerSuccess) {
                    if (officerStatus) {
                        sender.sendMessage(messages.get("clan_officer_promote_success_originator", Map.of("player", officerTarget.getName())));
                        if (officerTarget.isOnline()) {
                            ((Player) officerTarget).sendMessage(messages.get("clan_officer_promote_success_player", Map.of("player", sender.getName(), "clan", tag)));
                        }
                    } else {
                        sender.sendMessage(messages.get("clan_officer_demote_success_originator", Map.of("player", officerTarget.getName())));
                        if (officerTarget.isOnline()) {
                            ((Player) officerTarget).sendMessage(messages.get("clan_officer_demote_success_player", Map.of("player", sender.getName(), "clan", tag)));
                        }
                    }
                } else {
                    sender.sendMessage(messages.get("clan_officer_error_generic", null));
                }
                break;

            case "setarmor":

                if (!(sender instanceof Player)) {
                    sender.sendMessage(messages.get("clan_error_player", null));
                    return true;
                }

                Player armorPlayer = (Player) sender;
                ItemStack chestplate = armorPlayer.getInventory().getChestplate();
                ItemStack leggings = armorPlayer.getInventory().getLeggings();
                ItemStack boots = armorPlayer.getInventory().getBoots();

                if (chestplate == null || leggings == null || boots == null) {
                    sender.sendMessage(messages.get("clan_setarmor_error_missing", null));
                    return true;
                }

                if (manager.setClanArmor(tag, chestplate, leggings, boots)) {
                    sender.sendMessage(messages.get("clan_setarmor_success", null));
                    this.plugin.getDisplayManager().resetDisplays();
                } else {
                    sender.sendMessage(messages.get("clan_setarmor_error_generic", null));
                }
                break;

            case "setbanner":

                if (!(sender instanceof Player)) {
                    sender.sendMessage(messages.get("clan_error_player", null));
                    return true;
                }

                Player bannerPlayer = (Player) sender;
                ItemStack banner = bannerPlayer.getInventory().getItemInMainHand();

                if (banner == null || !banner.getType().toString().contains("BANNER")) {
                    sender.sendMessage(messages.get("clan_setbanner_error_missing", null));
                    return true;
                }

                if (manager.setBanner(tag, banner)) {
                    sender.sendMessage(messages.get("clan_setbanner_success", null));
                    this.plugin.getDisplayManager().resetDisplays();
                } else {
                    sender.sendMessage(messages.get("clan_setbanner_error_generic", null));
                }
                break;

            case "settag":

                if (args.length < 4) {
                    sender.sendMessage(messages.get("clan_settag_error_invalid_input", null));
                    return true;
                }

                String coloredTag = args[3];

                if (manager.setColorTag(tag, coloredTag)) {
                    sender.sendMessage(messages.get("clan_settag_success", null));
                    this.plugin.getDisplayManager().resetDisplays();
                } else {
                    sender.sendMessage(messages.get("clan_settag_error_generic", null));
                }
                break;

            case "ally":

                if (args.length < 4) {
                    sender.sendMessage(messages.get("clan_ally_error_usage", null));
                    return true;
                }

                String allyAction = args[3];

                if (allyAction.equalsIgnoreCase("dissolve")) {
                    if (!manager.hasAlly(tag)) {
                        sender.sendMessage(messages.get("clan_ally_error_no_alliance", null));
                        return true;
                    }

                    String ally = manager.getAlly(tag);

                    if (manager.removeAlliance(tag, ally)) {
                        sender.sendMessage(messages.get("clan_ally_dissolve_success", Map.of("ally", ally)));

                        // Notify the ally clan leader if online
                        UUID allyLeaderUUID = manager.getLeader(ally);
                        if (allyLeaderUUID != null) {
                            OfflinePlayer allyLeader = Bukkit.getOfflinePlayer(allyLeaderUUID);
                            if (allyLeader.isOnline()) {
                                ((Player) allyLeader).sendMessage(messages.get("clan_ally_dissolve_notification", Map.of("clan", tag)));
                            }
                        }
                    } else {
                        sender.sendMessage(messages.get("clan_ally_error_generic", null));
                    }
                    return true;
                }

                if (allyAction.equalsIgnoreCase("propose")) {
                    if (args.length < 5) {
                        sender.sendMessage(messages.get("clan_ally_error_no_clan_specified", null));
                        return true;
                    }

                    String targetClan = args[4];

                    if (!manager.getClans().stream().anyMatch(targetClan::equalsIgnoreCase)) {
                        sender.sendMessage(messages.get("clan_error_clan_doesnt_exist", null));
                        return true;
                    }

                    if (manager.hasAlly(tag)) {
                        sender.sendMessage(messages.get("clan_error_youre_already_allied", null));
                        return true;
                    }

                    if (manager.hasAlly(targetClan)) {
                        sender.sendMessage(messages.get("clan_error_theyre_already_allied", null));
                        return true;
                    }

                    if (tag.equalsIgnoreCase(targetClan)) {
                        sender.sendMessage(messages.get("clan_ally_error_generic", null));
                        return true;
                    }

                    UUID targetLeaderUUID = manager.getLeader(targetClan);
                    if (targetLeaderUUID == null) {
                        sender.sendMessage(messages.get("clan_error_clan_doesnt_exist", null));
                        return true;
                    }

                    OfflinePlayer targetLeader = Bukkit.getOfflinePlayer(targetLeaderUUID);
                    if (!targetLeader.isOnline()) {
                        sender.sendMessage(messages.get("clan_ally_error_generic", null));
                        return true;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(messages.get("clan_error_player", null));
                        return true;
                    }

                    plugin.getInviteManager().addRequest(Request.RequestType.ALLIANCE, (Player) targetLeader, (Player) sender, tag);
                    return true;
                }

                sender.sendMessage(messages.get("clan_ally_error_usage", null));
                break;

            default:
                sender.sendMessage(messages.get("clan_manage_error_args", null));
                break;

        }
        this.plugin.getDisplayManager().resetDisplays();
        return true;
    }
}
