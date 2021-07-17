package com.wasted_ticks.featherclans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Handler implements CommandExecutor {

    private static HashMap<String, CommandExecutor> commands;

    public Handler() {
        commands = new HashMap<>();
    }

    public void register(String subCommand, CommandExecutor executor) {
        commands.put(subCommand, executor);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0) {
            commands.get("help").onCommand(sender, command, label, args);
        } else {
            if(commands.containsKey(args[0].toLowerCase())) {
                commands.get(args[0].toLowerCase()).onCommand(sender, command, label, args);
            } else {
                sender.sendMessage("Invalid clan command specified.");
            }
        }
        return true;
    }

}
