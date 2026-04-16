package dev.zerek.featherclans.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Request {

    public enum RequestType {MEMBERSHIP, ALLIANCE, RALLY}

    private final RequestType type;
    private final Player target;
    private final Player originator;
    private final String tag;
    private final Location location;


    public Request(RequestType type, Player target, Player originator, String tag) {
        this(type, target, originator, tag, null);
    }

    public Request(RequestType type, Player target, Player originator, String tag, Location location) {
        this.type = type;
        this.target = target;
        this.originator = originator;
        this.tag = tag;
        this.location = location;
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

    public Location getLocation() {
        return this.location;
    }
}