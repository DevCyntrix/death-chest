package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.HologramOptions;
import com.github.devcyntrix.deathchest.controller.HologramController;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.github.devcyntrix.deathchest.tasks.HologramRunnable;
import com.github.devcyntrix.deathchest.util.ChestView;
import com.github.devcyntrix.hologram.api.Hologram;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.Map;

public class HologramView implements ChestView {

    private final DeathChestPlugin plugin;
    private final HologramController controller;
    private final HologramOptions options;

    private final PlaceholderController placeHolderController;

    public HologramView(DeathChestPlugin plugin, HologramController controller, HologramOptions options, PlaceholderController placeHolderController) {
        this.plugin = plugin;
        this.controller = controller;
        this.options = options;
        this.placeHolderController = placeHolderController;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        Hologram hologram = controller.spawnHologram(model.getLocation().clone().add(0.5, options.height(), 0.5));
        model.setHologram(hologram);

        Map<String, HologramTextLine> blueprints = new LinkedHashMap<>(options.lines().size());
        options.lines().forEach(line -> blueprints.put(line, hologram.appendLine(placeHolderController.replace(model, line)))); // A map of blueprints

        if (blueprints.isEmpty())
            return;

        plugin.debug(0, "Starting hologram updater...");
        BukkitTask bukkitTask = new HologramRunnable(plugin, model, blueprints, placeHolderController).runTaskTimerAsynchronously(plugin, 20, 20);
        model.getTasks().add(bukkitTask::cancel);
    }

    @Override
    public void onDestroy(DeathChestModel model) {
        Hologram hologram = model.getHologram();
        if (hologram == null)
            return;
        hologram.delete();
        model.setHologram(null);
    }

    @Override
    public void onLoad(DeathChestModel model) {
        onCreate(model);
    }

    @Override
    public void onUnload(DeathChestModel model) {
        onDestroy(model);
    }
}
