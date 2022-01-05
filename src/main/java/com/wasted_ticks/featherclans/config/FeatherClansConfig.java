package com.wasted_ticks.featherclans.config;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class FeatherClansConfig {

    private final FeatherClans plugin;
    private FileConfiguration config;

    /* SETTINGS */
    private boolean economyEnabled;
    private double economyCreationPrice;
    private double economyInvitePrice;
    private double economySetHomePrice;

    private boolean cleanupEnabled;
    private int cleanupArchiveDays;
    private int cleanupDeleteDays;

    private int clanInviteTimeout;
    private int clanTeleportDelaySeconds;
    private int clanMinTagSize;
    private int clanMaxTagSize;
    private int clanMaxMembers;

    private boolean useMySQL;
    private boolean mysqlEnabled;
    private String mysqlUsername;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlPassword;
    private String mysqlDatabase;

    private List<String> denyTags;

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
        this.economySetHomePrice = config.getDouble("settings.economy.set-home_price");

        this.cleanupEnabled = config.getBoolean("settings.use_clean_up");
        this.cleanupArchiveDays = config.getInt("settings.clean_up.archive_days");
        this.cleanupDeleteDays = config.getInt("settings.clean_up.delete_days");

        this.clanInviteTimeout = config.getInt("settings.clan.invite_timeout");
        this.clanTeleportDelaySeconds = config.getInt("settings.clan.teleport_delay_seconds");
        this.clanMinTagSize = config.getInt("settings.clan.min_tag_size");
        this.clanMaxTagSize = config.getInt("settings.clan.max_tag_size");
        this.clanMaxMembers = config.getInt("settings.clan.max_members");

        this.mysqlEnabled = config.getBoolean("settings.mysql.enabled");
        /* mysql stuff */
        this.mysqlUsername = config.getString("settings.mysql.username");
        this.mysqlHost = config.getString("settings.mysql.host");
        this.mysqlPort = config.getInt("settings.mysql.port");
        this.mysqlPassword = config.getString("settings.mysql.password");
        this.mysqlDatabase = config.getString("settings.mysql.database");

        this.denyTags = config.getStringList("settings.deny_tags");
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
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

    public boolean isCleanupEnabled() {
        return cleanupEnabled;
    }

    public int getCleanupArchiveDays() {
        return cleanupArchiveDays;
    }

    public int getCleanupDeleteDays() {
        return cleanupDeleteDays;
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

    public boolean isUseMySQL() {
        return useMySQL;
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

    public void setEconomyEnabled(boolean economyEnabled) {
        this.economyEnabled = economyEnabled;
    }
}
