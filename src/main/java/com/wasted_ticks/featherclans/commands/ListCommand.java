package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.util.ChatUtil;
import com.wasted_ticks.featherclans.util.TableUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public ListCommand(FeatherClans plugin) {
        this.plugin  = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;
            List<Clan> clans = plugin.getClanManager().getClans();

            if(clans.isEmpty()) {
                player.sendMessage(messages.get("clan_list_no_clans"));
            } else {
                player.sendMessage(messages.get("clan_pre_line"));
                player.sendMessage("");
                player.sendMessage(messages.get("clan_list_total") + clans.size());
                player.sendMessage("");
                TableUtil table = new TableUtil( "Tag", "Members");
                for (Clan clan: clans) {
                    List<OfflinePlayer> members = plugin.getClanManager().getOfflinePlayersByClan(clan);
                    table.addRow((String) clan.get("tag"),  String.valueOf(members.size()));
                }

                for (String line: table.generate()) {
                    player.sendMessage(line);
                }
                player.sendMessage(messages.get("clan_line"));

            }

        }
        return false;
    }
}
