package com.github.devcyntrix.deathchest.feature.hologram;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestListener;
import com.github.devcyntrix.deathchest.config.HologramOptions;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import com.github.devcyntrix.deathchest.feature.placeholder.PlaceholderService;
import com.github.devcyntrix.hologram.api.Hologram;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.Map;

public class HologramChestListener implements ChestListener {

    private final DeathChestPlugin plugin;
    private final HologramService controller;
    private final HologramOptions options;

    private final PlaceholderService placeHolderService;

    public HologramChestListener(DeathChestPlugin plugin, HologramService controller, HologramOptions options, PlaceholderService placeHolderService) {
        this.plugin = plugin;
        this.controller = controller;
        this.options = options;
        this.placeHolderService = placeHolderService;
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
        options.lines().forEach(line -> blueprints.put(hologram.appendLine(placeHolderService.replace(model, line)), line)); // A map of blueprints

        if (blueprints.isEmpty())
            return;

        plugin.debug(0, "Starting hologram updater...");
        BukkitTask bukkitTask = new HologramRunnable(plugin, model, blueprints, placeHolderService).runTaskTimerAsynchronously(plugin, 20, 20);
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
