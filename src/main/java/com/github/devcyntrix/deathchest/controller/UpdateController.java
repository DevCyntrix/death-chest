package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.util.UpdateChecker;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UpdateController implements Closeable {

    private String newestVersion;
    private final BukkitTask updateScheduler;

    private final List<Consumer<String>> subscriberList = new ArrayList<>();

    @Inject
    public UpdateController(DeathChestPlugin plugin) {
        UpdateChecker checker = new UpdateChecker(plugin, DeathChestPlugin.RESOURCE_ID);
        updateScheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String version = checker.getPublishedVersion();
            if (version == null)
                return;
            if (plugin.getDescription().getVersion().equals(version))
                return;
            this.newestVersion = version;
            subscriberList.forEach(subscriber -> subscriber.accept(newestVersion));
        }, 0, 20 * 60 * 30); // Every 30 minutes

    }

    public void subscribe(@NotNull Consumer<String> subscriber) {
        subscriberList.add(subscriber);
    }

    public String getNewestVersion() {
        return newestVersion;
    }

    @Override
    public void close() {
        if (updateScheduler != null) {
            updateScheduler.cancel();
        }
    }
}
