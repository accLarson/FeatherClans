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

    private Banner banner;
    private ArmorStand armorStand;
    private Sign sign;

    public DisplayManager(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public void createDisplay() {
        Display display = new Display(this.banner,this.armorStand,this.sign);
        if (plugin.getClanManager().createDisplayRecord(banner, armorStand, sign)) this.displays.add(display);

        this.banner = null;
        this.armorStand = null;
        this.sign = null;
    }

    public void storeDisplayInMemory(Banner banner, ArmorStand armorStand, Sign sign) {
        displays.add(new Display(banner, armorStand, sign));
    }
}
