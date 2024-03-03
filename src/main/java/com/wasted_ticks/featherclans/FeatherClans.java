package com.wasted_ticks.featherclans;

import com.wasted_ticks.featherclans.commands.*;
import com.wasted_ticks.featherclans.commands.completers.ClanTabCompleter;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import com.wasted_ticks.featherclans.listeners.EntityDamageByEntityEventListener;
import com.wasted_ticks.featherclans.listeners.PlayerDeathListener;
import com.wasted_ticks.featherclans.listeners.PlayerJoinListener;
import com.wasted_ticks.featherclans.listeners.ProjectileHitEventListener;
import com.wasted_ticks.featherclans.managers.*;
import com.wasted_ticks.featherclans.placeholders.FeatherClansPlaceholderExpansion;
import com.wasted_ticks.featherclans.util.ActivityUtil;
import com.wasted_ticks.featherclans.util.ColorTagUtil;
import com.wasted_ticks.featherclans.util.PaginateUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class FeatherClans extends JavaPlugin {

    private FeatherClans plugin;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private InviteManager inviteManager;
    private FriendlyFireManager friendlyFireManager;
    private PVPScoreManager pvpScoreManager;
    private PaginateUtil paginateUtil;
    private ActivityUtil activityUtil;
    private ColorTagUtil colorTagUtil;
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
        this.pvpScoreManager = new PVPScoreManager(plugin);
        this.inviteManager = new InviteManager(plugin);
        this.paginateUtil = new PaginateUtil(plugin);
        this.activityUtil = new ActivityUtil(plugin);
        this.colorTagUtil = new ColorTagUtil(plugin);

        if (this.config.isEconomyEnabled()) {
            if (!setupEconomy()) {
                plugin.getLogger().severe("Unable to hook into vault, economy functions will be disabled.");
                this.config.setEconomyEnabled(false);
            }
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            plugin.getLogger().info("Hooking into Placeholder API.");
            new FeatherClansPlaceholderExpansion(this).register();
        }

        this.registerCommands();
        this.getServer().getPluginManager().registerEvents(new EntityDamageByEntityEventListener(plugin), this);
        this.getServer().getPluginManager().registerEvents(new ProjectileHitEventListener(plugin), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin), this);
        this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(plugin),this);

        activityUtil.inactiveStatusCheck(clanManager.getAllClanMembers());
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
        return true;
    }

    @Override
    public void onDisable() {
        this.databaseManager.close();
    }

    public void reload() {
        this.config = new FeatherClansConfig(plugin);
        this.messages = new FeatherClansMessages(plugin);
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

    public PVPScoreManager getPVPScoreManager() {
        return this.pvpScoreManager;
    }

    public PaginateUtil getPaginateUtil() {
        return this.paginateUtil;
    }

    public ActivityUtil getActivityUtil() {
        return this.activityUtil;
    }

    public ColorTagUtil getColorTagUtil() {
        return this.colorTagUtil;
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
        handler.register("assignofficer", new AssignOfficerCommand(plugin));
        handler.register("dismissofficer", new DismissOfficerCommand(plugin));
        handler.register("colortag",new ColorTagCommand(plugin));

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

    }
}
