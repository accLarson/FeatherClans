package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConferCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ConferCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("ConferCommand");
        if(sender instanceof Player) {

            Player originator = (Player) sender;
            boolean leader = plugin.getClanManager().isOfflinePlayerLeader(originator);

            if(leader) {

                if (args.length != 2) {
                    originator.sendMessage("Error: No player specified to confer clan to.");
                    return false;
                }

                Player player = Bukkit.getPlayer(args[1]);

                if(player == null) {
                    originator.sendMessage("Error: Unable to resolve player name.");
                    return false;
                }

                Clan clan = this.plugin.getClanManager().getClanByOfflinePlayer(originator);
                if(this.plugin.getClanManager().isOfflinePlayerInSpecificClan(player, clan)) {
                    boolean successful = this.plugin.getClanManager().setClanLeader(clan, player);
                    if(successful) {
                        originator.sendMessage("You have conferred leadership of your clan to: " + player.getName());
                        player.sendMessage(originator.getName() + " has conferred leadership of " + clan.getString("tag") + " to you.");
                        return true;
                    }
                } else {
                    originator.sendMessage("Error: Player must be in your clan.");
                    return false;
                }

            } else {
                originator.sendMessage(messages.get("clan_error_leader"));
                return false;
            }
        }
        return false;
    }
}
