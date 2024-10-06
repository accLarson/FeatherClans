package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartnerChatLockCommand implements CommandExecutor {
    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public PartnerChatLockCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.partnerchatlock")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getMembershipManager().isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_partnerchatlock_error_not_in_clan", null));
            return true;
        }

        String clan = plugin.getMembershipManager().getClanByOfflinePlayer(player);
        if (!plugin.getClanManager().hasPartner(clan)) {
            player.sendMessage(messages.get("clan_partnerchat_no_partner", null));
            return true;
        }

        if (plugin.getClanChatLockManager().isInPartnerChatLock(player)) {
            plugin.getClanChatLockManager().removePlayerFromPartnerChat(player);
            player.sendMessage(messages.get("clan_partnerchatlock_disabled", null));
        } else {
            plugin.getClanChatLockManager().addPlayerToPartnerChat(player);
            player.sendMessage(messages.get("clan_partnerchatlock_enabled", null));
        }

        return true;
    }
}
