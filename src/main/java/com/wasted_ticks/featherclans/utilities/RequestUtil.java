package com.wasted_ticks.featherclans.utilities;

import org.bukkit.entity.Player;

public class RequestUtil {

    // Define the enum within the RequestUtil class
    public enum RequestType {
        CLAN_INVITE,
        PARTNERSHIP_INVITE
    }

    private String tag;
    private Player originator;
    private RequestType requestType;

    // Update the constructor to use the new enum
    public RequestUtil(String tag, Player originator, RequestType requestType) {
        this.tag = tag;
        this.originator = originator;
        this.requestType = requestType;
    }

    public String getClan() {
        return tag;
    }

    public Player getOriginator() {
        return originator;
    }

    public RequestType getType() {
        return requestType;
    }

}
