package com.wasted_ticks.featherclans;

import com.wasted_ticks.featherclans.commands.*;
import com.wasted_ticks.featherclans.commands.completers.ClanTabCompleter;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.data.Clan;
import com.wasted_ticks.featherclans.data.ClanMember;
import com.wasted_ticks.featherclans.managers.ClanManager;
import com.wasted_ticks.featherclans.managers.DatabaseManager;
import com.wasted_ticks.featherclans.managers.InviteManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class FeatherClans extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private FeatherClans plugin;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private InviteManager inviteManager;
    private FeatherClansConfig config;
    private FeatherClansMessages messages;

    @Override
    public void onEnable() {

        plugin = this;

        this.databaseManager = new DatabaseManager(plugin);
        this.clanManager = new ClanManager(plugin);
        this.config = new FeatherClansConfig(plugin);
        this.messages = new FeatherClansMessages(plugin);
        this.inviteManager = new InviteManager(plugin);
        registerCommands();

    }

    @Override
    public void onDisable() {
        this.databaseManager.close();
    }

    public void reload() {
        this.config = new FeatherClansConfig(plugin);
        this.messages = new FeatherClansMessages(plugin);
    }

    public Logger getLog() {
        return logger;
    }

    public ClanManager getClanManager() {
        return this.clanManager;
    }

    public InviteManager getInviteManager() { return this.inviteManager; }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public FeatherClansConfig getFeatherClansConfig() { return this.config; }

    public FeatherClansMessages getFeatherClansMessages() {
        return this.messages;
    }

    private void registerCommands() {

        Handler handler = new Handler(plugin);

        handler.register("create", new CreateCommand(plugin));
        handler.register("invite", new InviteCommand(plugin));
        handler.register("kick", new KickCommand(plugin));
        handler.register("accept", new AcceptCommand(plugin));
        handler.register("decline", new DeclineCommand(plugin));
        handler.register("sethome", new SetHomeCommand(plugin));
        handler.register("home", new HomeCommand(plugin));
        handler.register("confer", new ConferCommand(plugin));
        handler.register("disband", new DisbandCommand(plugin));
        handler.register("resign", new ResignCommand(plugin));
        handler.register("roster", new RosterCommand(plugin));
        handler.register("chat", new ChatCommand(plugin));
        handler.register("list",new ListCommand(plugin));
        handler.register("leaderboard",new LeaderboardCommand(plugin));
        handler.register("help", new HelpCommand(plugin));
        handler.register("sudo", new SudoCommand(plugin));
        handler.register("reload", new ReloadCommand(plugin));

        PluginCommand command = this.getCommand("clan");

        if(command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(new ClanTabCompleter(plugin));
        }
    }
}
