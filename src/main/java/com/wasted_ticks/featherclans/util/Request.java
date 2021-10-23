package com.wasted_ticks.featherclans.util;

import com.wasted_ticks.featherclans.data.Clan;
import org.bukkit.entity.Player;

public class Request {

    private Clan clan;
    private Player originator;

    public Request(Clan clan, Player originator) {
        this.clan = clan;
        this.originator = originator;
    }

    public Clan getClan() {
        return clan;
    }

    public Player getOriginator() {
        return originator;
    }

}
