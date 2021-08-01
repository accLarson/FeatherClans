package com.wasted_ticks.featherclans.config;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class FeatherClansConfig {

    private final FeatherClans plugin;
    private FileConfiguration config;

    /* SETTINGS */
    private boolean useEconomy;
    private double creationPrice;
    private double invitePrice;
    private double setHomePrice;
    private boolean useCleanUp;
    private int archiveDays;
    private int deleteDays;
    private int inviteTimeout;
    private int teleportDelaySeconds;
    private int tagSize;
    private int maxMembers;
    private boolean useMySQL;
    /* mysql stuff */
    private List<String> denyTags;

    public FeatherClansConfig(FeatherClans plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        config = this.plugin.getConfig();
        this.loadConfig();
    }

    private void loadConfig() {

        this.useEconomy = config.getBoolean("settings.use_economy");
        this.creationPrice = config.getDouble("settings.economy.creation_price");
        this.invitePrice = config.getDouble("settings.economy.invite_price");
        this.setHomePrice = config.getDouble("settings.economy.set-home_price");

        this.useCleanUp = config.getBoolean("settings.use_clean_up");
        this.archiveDays = config.getInt("settings.clean_up.archive_days");
        this.deleteDays = config.getInt("settings.clean_up.delete_days");

        this.inviteTimeout = config.getInt("settings.clan.invite_timeout");
        this.teleportDelaySeconds = config.getInt("settings.clan.teleport_delay_seconds");
        this.tagSize = config.getInt("settings.clan.tag_size");
        this.maxMembers = config.getInt("settings.clan.max_members");

        this.useMySQL = config.getBoolean("settings.use_mysql");
        /* mysql stuff */

        this.denyTags = config.getStringList("settings.deny_tags");
    }

    public boolean isUseEconomy() {
        return useEconomy;
    }

    public double getCreationPrice() {
        return creationPrice;
    }

    public double getInvitePrice() {
        return invitePrice;
    }

    public double getSetHomePrice() {
        return setHomePrice;
    }

    public boolean isUseCleanUp() {
        return useCleanUp;
    }

    public int getArchiveDays() {
        return archiveDays;
    }

    public int getDeleteDays() {
        return deleteDays;
    }

    public int getInviteTimeout() {
        return inviteTimeout;
    }

    public int getTeleportDelaySeconds() {
        return teleportDelaySeconds;
    }

    public int getTagSize() { return tagSize; }

    public int getMaxMembers() {
        return maxMembers;
    }

    public boolean isUseMySQL() {
        return useMySQL;
    }

    public List<String> getDenyTags() {
        return denyTags;
    }
}
