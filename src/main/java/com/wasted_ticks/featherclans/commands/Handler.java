package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Handler implements CommandExecutor {

    private static HashMap<String, CommandExecutor> commands;
    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public Handler(FeatherClans plugin) {
        commands = new HashMap<>();
        this.plugin = plugin;
        messages = this.plugin.getFeatherClansMessages();
    }

    public void register(String subCommand, CommandExecutor executor) {
        commands.put(subCommand, executor);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            commands.get("help").onCommand(sender, command, label, args);
        } else {
            if (commands.containsKey(args[0].toLowerCase())) {
                commands.get(args[0].toLowerCase()).onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(messages.get("clan_command_error", null));
            }
        }
        return true;
    }
}
