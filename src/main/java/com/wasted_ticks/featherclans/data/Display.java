package com.wasted_ticks.featherclans.data;

import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;

public class Display {

    private final Banner banner;
    private final ArmorStand armorStand;
    private final Sign sign;
    public Display (Banner banner, ArmorStand armorStand, Sign sign) {
        this.banner = banner;
        this.armorStand = armorStand;
        this.sign = sign;
    }


    public Banner getBanner() {
        return banner;
    }
    public ArmorStand getArmorStand() {
        return armorStand;
    }
    public Sign getSign() {
        return sign;
    }
}
