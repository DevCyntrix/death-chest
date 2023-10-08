package com.github.devcyntrix.deathchest.tasks;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class HologramRunnable extends BukkitRunnable {

    private final Plugin plugin;
    private final DeathChestModel chest;
    private final Map<String, HologramTextLine> blueprints;
    private final PlaceholderController controller;

    public HologramRunnable(Plugin plugin, DeathChestModel chest, Map<String, HologramTextLine> blueprints, PlaceholderController controller) {
        this.plugin = plugin;
        this.chest = chest;
        this.blueprints = blueprints;
        this.controller = controller;
    }

    @Override
    public void run() {
        // Updates the hologram lines
        blueprints.forEach((text, line) -> {
            Bukkit.getScheduler().runTask(plugin, () -> line.rename(controller.replace(chest, text)));
        });
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        this.blueprints.forEach((s, hologramTextLine) -> hologramTextLine.remove());
        super.cancel();
    }
}
