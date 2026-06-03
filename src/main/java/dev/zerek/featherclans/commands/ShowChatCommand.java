package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.managers.ChatToggleManager;
import dev.zerek.featherclans.managers.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShowChatCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final ChatToggleManager chatToggleManager;
    private final FeatherClansMessages messages;

    public ShowChatCommand(FeatherClans plugin) {
        this.clanManager = plugin.getClanManager();
        this.chatToggleManager = plugin.getChatToggleManager();
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("feather.clans.chat")) {
            player.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!clanManager.isOfflinePlayerInClan(player)) {
            player.sendMessage(messages.get("clan_showchat_no_clan", null));
            return true;
        }

        if (!chatToggleManager.isChatHidden(player.getUniqueId())) {
            player.sendMessage(messages.get("clan_showchat_already", null));
            return true;
        }

        chatToggleManager.setChatHidden(player.getUniqueId(), false);
        player.sendMessage(messages.get("clan_showchat", null));
        return true;
    }
}