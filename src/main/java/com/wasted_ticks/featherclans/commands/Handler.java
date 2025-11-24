package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Handler implements CommandExecutor {

    private static HashMap<String, CommandExecutor> commands;
    private final FeatherClans plugin;

    public Handler(FeatherClans plugin) {
        commands = new HashMap<>();
        this.plugin = plugin;
    }

    public void register(String subCommand, CommandExecutor executor) {
        commands.put(subCommand, executor);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (label.equalsIgnoreCase("cc")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "chat";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return commands.get("chat").onCommand(sender, command, label, newArgs);
        }
        
        if (label.equalsIgnoreCase("cac")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "allychat";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return commands.get("allychat").onCommand(sender, command, label, newArgs);
        }
        
        if (label.equalsIgnoreCase("cctoggle")) {
            String[] newArgs = new String[1];
            newArgs[0] = "chattoggle";
            return commands.get("chattoggle").onCommand(sender, command, label, newArgs);
        }
        
        if (label.equalsIgnoreCase("cactoggle")) {
            String[] newArgs = new String[1];
            newArgs[0] = "allychattoggle";
            return commands.get("allychattoggle").onCommand(sender, command, label, newArgs);
        }
        
        if (label.equalsIgnoreCase("ff")) {
            String[] newArgs = new String[1];
            newArgs[0] = "friendlyfire";
            return commands.get("friendlyfire").onCommand(sender, command, label, newArgs);
        }
        
        if (label.equalsIgnoreCase("ch")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "home";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return commands.get("home").onCommand(sender, command, label, newArgs);
        }
        
        if (label.equalsIgnoreCase("cmcc")) {
            String[] newArgs = new String[args.length + 2];
            newArgs[0] = "manage";
            newArgs[1] = args[0];
            newArgs[2] = "chat";
            System.arraycopy(args, 1, newArgs, 3, args.length - 1);
            return commands.get("manage").onCommand(sender, command, label, newArgs);
        }
        
        // Handle regular /clan commands
        if (args.length == 0) {
            commands.get("help").onCommand(sender, command, label, args);
        } else {
            if (commands.containsKey(args[0].toLowerCase())) {
                commands.get(args[0].toLowerCase()).onCommand(sender, command, label, args);
            } else {
                sender.sendMessage("Unknown clan command."); // Since messages removed, fallback message
            }
        }
        return true;
    }

}
