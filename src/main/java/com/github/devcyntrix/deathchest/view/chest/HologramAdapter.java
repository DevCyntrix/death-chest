package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.api.hologram.Hologram;
import com.github.devcyntrix.deathchest.api.hologram.HologramTextLine;
import com.github.devcyntrix.deathchest.config.HologramOptions;
import com.github.devcyntrix.deathchest.controller.HologramController;
import com.github.devcyntrix.deathchest.controller.PlaceHolderController;
import com.github.devcyntrix.deathchest.tasks.HologramRunnable;
import com.github.devcyntrix.deathchest.util.ChestAdapter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.Map;

public class HologramAdapter implements ChestAdapter {

    private final Plugin plugin;
    private final HologramController controller;
    private final HologramOptions options;

    private final PlaceHolderController placeHolderController;

    public HologramAdapter(Plugin plugin, HologramController controller, HologramOptions options, PlaceHolderController placeHolderController) {
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
