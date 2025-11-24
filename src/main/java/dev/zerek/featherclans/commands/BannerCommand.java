package dev.zerek.featherclans.commands;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansMessages;
import dev.zerek.featherclans.managers.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class BannerCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;


    public BannerCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("feather.clans.banner")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(messages.get("clan_banner_error_invalid_arg_length", null));
            return true;
        }

        String tag = args[1];
        ClanManager manager = plugin.getClanManager();
        if (!manager.getClans().stream().anyMatch(tag::equalsIgnoreCase)) {
            sender.sendMessage(messages.get("clan_banner_error_unresolved_clan", null));
            return true;
        }

        ItemStack stack = manager.getBanner(tag.toLowerCase());
        Player player = (Player) sender;
        player.getInventory().addItem(stack);

        return true;
    }

}
