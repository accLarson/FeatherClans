package com.wasted_ticks.featherclans.utilities;

import org.bukkit.entity.Player;

public class RequestUtility {

    private String tag;
    private Player originator;

    public RequestUtility(String tag, Player originator) {
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
