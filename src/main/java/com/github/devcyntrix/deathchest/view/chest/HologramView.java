package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestView;
import com.github.devcyntrix.deathchest.config.HologramOptions;
import com.github.devcyntrix.deathchest.controller.HologramController;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.github.devcyntrix.deathchest.tasks.HologramRunnable;
import com.github.devcyntrix.hologram.api.Hologram;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
        Chunk chunk = model.getLocation().getChunk();
        boolean loaded = chunk.isLoaded();
        if (!loaded)
            chunk.load();

        Location holoPos = model.getLocation().clone().add(0.5, options.height(), 0.5);
        Hologram hologram = controller.spawnHologram(holoPos, options.lineHeight());
        model.setHologram(hologram);

        Map<HologramTextLine, String> blueprints = new LinkedHashMap<>(options.lines().size());
        options.lines().forEach(line -> blueprints.put(hologram.appendLine(placeHolderController.replace(model, line)), line)); // A map of blueprints

        if (blueprints.isEmpty())
            return;

        plugin.debug(0, "Starting hologram updater...");
        BukkitTask bukkitTask = new HologramRunnable(plugin, model, blueprints, placeHolderController).runTaskTimerAsynchronously(plugin, 20, 20);
        model.getTasks().add(bukkitTask::cancel);

        if (!loaded)
            chunk.unload();
    }

    @Override
    public void onDestroy(DeathChestModel model) {
        Hologram hologram = model.getHologram();
        if (hologram == null)
            return;

        Chunk chunk = hologram.getLocation().getChunk();
        boolean loaded = chunk.isLoaded();
        if (!loaded)
            chunk.load();

        hologram.delete();

        if (!loaded)
            chunk.unload();
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
