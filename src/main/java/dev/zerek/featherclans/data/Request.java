package dev.zerek.featherclans.data;

import org.bukkit.entity.Player;

public class Request {

    public enum RequestType {MEMBERSHIP, ALLIANCE}

    private final RequestType type;
    private final Player target;
    private final Player originator;
    private final String tag;


    public Request(RequestType type, Player target, Player originator, String tag) {
        this.type = type;
        this.target = target;
        this.originator = originator;
        this.tag = tag;
    }

    public RequestType getType() {
        return this.type;
    }

    public Player getTarget() {
        return this.target;
    }

        public Player getOriginator() {
        return this.originator;
    }

    public String getClan() {
        return this.tag;
    }
}