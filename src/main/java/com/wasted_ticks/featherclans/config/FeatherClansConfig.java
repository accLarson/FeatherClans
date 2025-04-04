package com.wasted_ticks.featherclans.config;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class FeatherClansConfig {

    private final FeatherClans plugin;
    private FileConfiguration config;
    private Location displayLocation;
    private String displayFacing;
    private int displayCount;
    private String signType;

    /* SETTINGS */
    private boolean economyEnabled;
    private double economyCreationPrice;
    private double economyInvitePrice;
    private double economySetHomePrice;
    private double economySetArmorPrice;
    private double economySetTagPrice;
    private double economySetBannerPrice;

    private boolean cleanupEnabled;
    private int cleanupArchiveDays;
    private int cleanupDeleteDays;

    private int clanActiveMembersRequirement;
    private int clanInactiveDaysThreshold;
    private int clanInviteTimeout;
    private int clanTeleportDelaySeconds;
    private int clanMinTagSize;
    private int clanMaxTagSize;
    private int clanMaxMembers;

    /* INDICATORS */
    private String officerIndicator;
    private String leaderIndicator;

    private boolean mysqlEnabled;
    private String mysqlUsername;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlPassword;
    private String mysqlDatabase;

    private List<String> denyTags;

    private int linesPerPage;

    public FeatherClansConfig(FeatherClans plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        config = this.plugin.getConfig();
        this.loadConfig();
    }

    private void loadConfig() {

        this.economyEnabled = config.getBoolean("settings.economy.enabled");
        this.economyCreationPrice = config.getDouble("settings.economy.creation_price");
        this.economyInvitePrice = config.getDouble("settings.economy.invite_price");
        this.economySetHomePrice = config.getDouble("settings.economy.set_home_price");
        this.economySetArmorPrice = config.getDouble("settings.economy.set_armor_price");
        this.economySetTagPrice = config.getDouble("settings.economy.set_tag_price");
        this.economySetBannerPrice = config.getDouble("settings.economy.set_banner_price");

        this.cleanupEnabled = config.getBoolean("settings.clean_up.enabled");
        this.cleanupArchiveDays = config.getInt("settings.clean_up.archive_days");
        this.cleanupDeleteDays = config.getInt("settings.clean_up.delete_days");

        this.clanActiveMembersRequirement = config.getInt("settings.clan.active_members_requirement");
        this.clanInactiveDaysThreshold = config.getInt("settings.clan.inactive_days_threshold");
        this.clanInviteTimeout = config.getInt("settings.clan.invite_timeout");
        this.clanTeleportDelaySeconds = config.getInt("settings.clan.teleport_delay_seconds");
        this.clanMinTagSize = config.getInt("settings.clan.min_tag_size");
        this.clanMaxTagSize = config.getInt("settings.clan.max_tag_size");
        this.clanMaxMembers = config.getInt("settings.clan.max_members");

        /* Load indicator settings */
        this.officerIndicator = config.getString("settings.indicators.officer");
        this.leaderIndicator = config.getString("settings.indicators.leader");

        this.mysqlEnabled = config.getBoolean("settings.mysql.enabled");
        this.mysqlUsername = config.getString("settings.mysql.username");
        this.mysqlHost = config.getString("settings.mysql.host");
        this.mysqlPort = config.getInt("settings.mysql.port");
        this.mysqlPassword = config.getString("settings.mysql.password");
        this.mysqlDatabase = config.getString("settings.mysql.database");

        List<Double> coords = config.getDoubleList("settings.display.location");
        World world = Bukkit.getWorlds().get(0);
        this.displayLocation = new Location(world, coords.get(0), coords.get(1), coords.get(2));
        this.displayFacing = config.getString("settings.display.facing");
        this.displayCount = config.getInt("settings.display.count");
        this.signType = config.getString("settings.display.sign_type");

        this.denyTags = config.getStringList("settings.deny_tags");

        this.linesPerPage = config.getInt("page-formats.lines-per-page");
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public void setEconomyEnabled(boolean economyEnabled) {
        this.economyEnabled = economyEnabled;
    }

    public double getEconomyCreationPrice() {
        return economyCreationPrice;
    }

    public double getEconomyInvitePrice() {
        return economyInvitePrice;
    }

    public double getEconomySetHomePrice() {
        return economySetHomePrice;
    }

    public double getEconomySetArmorPrice() {
        return economySetArmorPrice;
    }

    public double getEconomySetTagPrice() {
        return economySetTagPrice;
    }

    public double getEconomySetBannerPrice() {
        return economySetBannerPrice;
    }

    public boolean isCleanupEnabled() {
        return cleanupEnabled;
    }

    public int getCleanupArchiveDays() {
        return cleanupArchiveDays;
    }

    public int getCleanupDeleteDays() {
        return cleanupDeleteDays;
    }

    public int getClanActiveMembersRequirement() {
        return clanActiveMembersRequirement;
    }

    public int getClanInactiveDaysThreshold() {
        return clanInactiveDaysThreshold;
    }

    public int getClanInviteTimeout() {
        return clanInviteTimeout;
    }

    public int getClanTeleportDelaySeconds() {
        return clanTeleportDelaySeconds;
    }

    public int getClanMinTagSize() {
        return clanMinTagSize;
    }

    public int getClanMaxTagSize() {
        return clanMaxTagSize;
    }

    public int getClanMaxMembers() {
        return clanMaxMembers;
    }

    public boolean isMysqlEnabled() {
        return mysqlEnabled;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public List<String> getDenyTags() {
        return denyTags;
    }

    public int getLinesPerPage() {
        return linesPerPage;
    }

    public Location getDisplayLocation() {
        return displayLocation;
    }

    public String getDisplayFacing() {
        return displayFacing;
    }

    public int getDisplayCount() {
        return displayCount;
    }

    public String getSignType() {
        return signType;
    }

    public String getOfficerIndicator() {
        return officerIndicator;
    }

    public String getLeaderIndicator() {
        return leaderIndicator;
    }
}
