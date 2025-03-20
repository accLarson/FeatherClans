package com.wasted_ticks.featherclans;

import com.wasted_ticks.featherclans.commands.*;
import com.wasted_ticks.featherclans.commands.completers.ClanTabCompleter;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.listeners.EntityDamageByEntityEventListener;
import com.wasted_ticks.featherclans.listeners.PlayerJoinListener;
import com.wasted_ticks.featherclans.listeners.ProjectileHitEventListener;
import com.wasted_ticks.featherclans.managers.*;
import com.wasted_ticks.featherclans.placeholders.FeatherClansPlaceholderExpansion;
import com.wasted_ticks.featherclans.utilities.PaginateUtility;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class FeatherClans extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private FeatherClans plugin;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private InviteManager inviteManager;
    private FriendlyFireManager friendlyFireManager;
    private ActiveManager activeManager;
    private DisplayManager displayManager;
    private PaginateUtility paginateUtility;
    private FeatherClansConfig config;
    private FeatherClansMessages messages;

    private Economy economy;

    @Override
    public void onEnable() {

        plugin = this;

        this.config = new FeatherClansConfig(plugin);
        this.messages = new FeatherClansMessages(plugin);
        this.databaseManager = new DatabaseManager(plugin);
        this.clanManager = new ClanManager(plugin);
        this.friendlyFireManager = new FriendlyFireManager();
        this.activeManager = new ActiveManager(plugin);
        this.displayManager = new DisplayManager(plugin);
        this.inviteManager = new InviteManager(plugin);
        this.paginateUtility = new PaginateUtility(plugin);

        if (this.config.isEconomyEnabled()) {
            if (!setupEconomy()) {
                plugin.getLog().severe("[FeatherClans] Unable to hook into vault, economy functions will be disabled.");
                this.config.setEconomyEnabled(false);
            }
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            plugin.getLog().info("[FeatherClans] Hooking into Placeholder API.");
            new FeatherClansPlaceholderExpansion(this).register();
        }

        this.registerCommands();
        this.getServer().getPluginManager().registerEvents(new EntityDamageByEntityEventListener(plugin), this);
        this.getServer().getPluginManager().registerEvents(new ProjectileHitEventListener(plugin), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin), this);
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> service = getServer().getServicesManager().getRegistration(Economy.class);
        if (service == null) {
            return false;
        }
        economy = service.getProvider();
        return economy != null;
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

    public InviteManager getInviteManager() {
        return this.inviteManager;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public FriendlyFireManager getFriendlyFireManager() {
        return this.friendlyFireManager;
    }

    public ActiveManager getActiveManager() {
        return this.activeManager;
    }

    public DisplayManager getDisplayManager() {
        return this.displayManager;
    }

    public PaginateUtility getPaginateUtil() {
        return this.paginateUtility;
    }

    public FeatherClansConfig getFeatherClansConfig() {
        return this.config;
    }

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
        handler.register("setarmor", new SetArmorCommand(plugin));
        handler.register("home", new HomeCommand(plugin));
        handler.register("confer", new ConferCommand(plugin));
        handler.register("disband", new DisbandCommand(plugin));
        handler.register("resign", new ResignCommand(plugin));
        handler.register("roster", new RosterCommand(plugin));
        handler.register("chat", new ChatCommand(plugin));
        handler.register("list", new ListCommand(plugin));
        handler.register("leaderboard", new LeaderboardCommand(plugin));
        handler.register("help", new HelpCommand(plugin));
        handler.register("reload", new ReloadCommand(plugin));
        handler.register("banner", new BannerCommand(plugin));
        handler.register("friendlyfire", new FriendlyFireCommand(plugin));
        handler.register("manage",new ManageCommand(plugin));

        PluginCommand command = this.getCommand("clan");

        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(new ClanTabCompleter(plugin));
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    public void disable() {
        this.getPluginLoader().disablePlugin(this);
    }
}
