package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements CommandExecutor {

    private final FeatherClans plugin;

    public HelpCommand(FeatherClans plugin) {
        this.plugin  = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            player.sendMessage(" /clan create <tag> — Create a new clan"); // just missing econ stuff
            player.sendMessage(" /clan invite <player> — Invite a player to your clan");
            player.sendMessage(" /clan kick <player> — Kick a player from your clan");
            player.sendMessage(" /clan accept — Accept invitation request");
            player.sendMessage(" /clan decline — Deny invitation request");
            player.sendMessage(" /clan sethome — Set clan home to current location"); // just missing econ stuff
            player.sendMessage(" /clan home — Teleport to your clan home"); // done
            player.sendMessage(" /clan confer <player> — Relinquish control of clan to a player");
            player.sendMessage(" /clan disband — Disband your clan"); //done
            player.sendMessage(" /clan resign — Resign from your clan"); // done
            player.sendMessage(" /clan roster <tag> — Show information about a clan");
            player.sendMessage(" /clan list — List all clans"); // done
            player.sendMessage(" /clan help — Display this message"); // done
        }

        return false;
    }
}
