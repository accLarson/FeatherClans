package dev.zerek.featherclans.tasks;

import dev.zerek.featherclans.FeatherClans;

/**
 * Builds the clan display row shortly after enable, once worlds and the active-clan state are ready.
 * The display chunks are held loaded via plugin chunk tickets, so this only runs once per start.
 */
public class InitiateTask implements Runnable {

    private final FeatherClans plugin;

    public InitiateTask(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getDisplayManager().load();
    }
}
