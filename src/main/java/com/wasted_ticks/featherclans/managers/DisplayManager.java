package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.data.Display;
import com.wasted_ticks.featherclans.utilities.SerializationUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class DisplayManager {

    private final FeatherClans plugin;
    private final List<Display> displays = new ArrayList<>();
    private final Map<Player,Integer> setupModePlayers = new HashMap<>();

    private Banner banner;
    private ArmorStand armorStand;
    private Sign sign;

    public DisplayManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public void addSetUpPlayer(Player player) {
        setupModePlayers.put(player,0);
    }

    public void nextStep(Player player) {
        int newStep = setupModePlayers.get(player) + 1;
        if (newStep <= 2) setupModePlayers.put(player, newStep);
        else {
            setupModePlayers.remove(player);
            this.createDisplay();
        }
    }

    public boolean isPlayerSettingDisplay(Player player) {
        return setupModePlayers.containsKey(player);
    }

    public void createDisplay() {
        Display display = new Display(this.banner,this.armorStand,this.sign);
        if (plugin.getClanManager().createDisplayRecord(banner, armorStand, sign)) this.displays.add(display);

        this.banner = null;
        this.armorStand = null;
        this.sign = null;
    }

    public void resetDisplays() {
        this.displays.clear();
        plugin.getClanManager().deleteDisplays();
    }

    public void storeDisplayInMemory(Banner banner, ArmorStand armorStand, Sign sign) {
        displays.add(new Display(banner, armorStand, sign));
    }

    public boolean isSettingBanner(Player player) {
        return setupModePlayers.get(player).equals(0);
    }

    public boolean isSettingArmorStand(Player player) {
        return setupModePlayers.get(player).equals(1);
    }

    public boolean isSettingSign(Player player) {
        return setupModePlayers.get(player).equals(2);
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public void setArmorStand(ArmorStand armorStand) {
        this.armorStand = armorStand;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }
}
