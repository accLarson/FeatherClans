package com.wasted_ticks.featherclans.utilities;

import com.wasted_ticks.featherclans.FeatherClans;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TeleportTimerUtil implements Runnable {

    private final int seconds;
    private final Runnable before;
    private final Runnable after;
    private final Consumer<TeleportTimerUtil> during;
    private final FeatherClans plugin;
    private int taskID;
    private int remaining;
    private Location location;

    /**
     * @param plugin
     * @param seconds
     * @param before
     * @param after
     * @param during
     */
    public TeleportTimerUtil(@NotNull FeatherClans plugin, int seconds, @Nullable Runnable before, @Nullable Runnable after, @NotNull Consumer<TeleportTimerUtil> during, @NotNull Location location) {
        this.plugin = plugin;
        this.seconds = seconds;
        this.before = before;
        this.after = after;
        this.during = during;
        this.remaining = seconds;
        this.location = location;
    }

    @Override
    public void run() {
        if (remaining <= 0) {
            if (after != null) after.run();
            Bukkit.getScheduler().cancelTask(taskID);
            return;
        }
        if (seconds == remaining && before != null) before.run();
        during.accept(this);
        remaining--;
    }

    public void start() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this, 0L, 20L);
    }

    public Location getStartLocation() {
        return this.location;
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    public int getRemainingSeconds() {
        return this.remaining;
    }

}
