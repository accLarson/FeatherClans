package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.util.Table;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListCommand implements CommandExecutor {

    private final FeatherClans plugin;

    public ListCommand(FeatherClans plugin) {
        this.plugin  = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;
            List<Clan> clans = plugin.getClanManager().getClans();

            if(clans.isEmpty()) {
                player.sendMessage("There are currently no clans to list.");
            } else {
                player.sendMessage("feather64 clan list -------------------------");
                player.sendMessage("");
                player.sendMessage("Total clans: " + clans.size());
                player.sendMessage("");
                Table table = new Table("Tag", "Members");
                for (Clan clan: clans) {
                    List<ClanMember> members = plugin.getClanManager().getClanMembersByClan(clan);
                    table.addRow((String) clan.get("tag"),  String.valueOf(members.size()));
                }

                for (String line: table.generate()) {
                    player.sendMessage(line);
                }
                player.sendMessage("---------------------------------------------");

            }

        }
        return false;
    }
}
