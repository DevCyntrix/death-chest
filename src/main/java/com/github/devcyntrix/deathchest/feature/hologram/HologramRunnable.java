package com.github.devcyntrix.deathchest.feature.hologram;

import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import com.github.devcyntrix.deathchest.feature.placeholder.PlaceholderService;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

@AllArgsConstructor
public class HologramRunnable extends BukkitRunnable {

    private final Plugin plugin;
    private final DeathChestModel chest;
    private final Map<HologramTextLine, String> blueprints;
    private final PlaceholderService controller;

    @Override
    public void run() {
        // Updates the hologram lines
        Bukkit.getScheduler().runTask(plugin, () -> {
            blueprints.forEach((line, text) -> {
                line.rename(controller.replace(chest, text));
            });
        });
    }
}
