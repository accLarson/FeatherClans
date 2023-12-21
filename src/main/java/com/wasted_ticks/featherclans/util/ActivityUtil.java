package com.wasted_ticks.featherclans.util;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.wasted_ticks.featherclans.managers.ClanManager;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class ActivityUtil {

    private final FeatherClans plugin;
    private FeatherClansConfig config;
    private ClanManager clanManager;

    public ActivityUtil(FeatherClans plugin) {
        this.plugin = plugin;
        this.init();
    }

    private void init(){
        this.config = this.plugin.getFeatherClansConfig();
        this.clanManager = this.plugin.getClanManager();
    }

    public void autoKickCheck(List<OfflinePlayer> clanMembers){

        clanMembers.forEach(member -> {
            if (clanManager.isOfflinePlayerLeader(member)) return;

            if (isInactive(member)) {
                String tag = clanManager.getClanByOfflinePlayer(member);
                clanManager.resignOfflinePlayer(member);
                plugin.getLogger().info(member.getName() + " was auto kicked from " + tag + " clan because they have not logged in for " + config.getAutoKickInactiveDays() + " days.");
            }

        });
    }

    public boolean isInactive(OfflinePlayer offlinePlayer) {
        int lastSeenInt = (int) ((System.currentTimeMillis() - offlinePlayer.getLastLogin()) / 86400000);
        return lastSeenInt >= config.getAutoKickInactiveDays();
    }

}
