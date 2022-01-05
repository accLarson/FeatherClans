package com.wasted_ticks.featherclans.util;

import org.bukkit.entity.Player;

public class RequestUtil {

    private String tag;
    private Player originator;

    public RequestUtil(String tag, Player originator) {
        this.tag = tag;
        this.originator = originator;
    }

    public String getClan() {
        return tag;
    }

    public Player getOriginator() {
        return originator;
    }

}
