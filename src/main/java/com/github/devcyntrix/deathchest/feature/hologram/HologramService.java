package com.github.devcyntrix.deathchest.feature.hologram;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.hologram.NativeHologram;
import com.github.devcyntrix.hologram.api.Hologram;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class HologramService implements com.github.devcyntrix.hologram.api.HologramService, Closeable {

    private final DeathChestPlugin plugin;
    private final Set<Hologram> holograms = new HashSet<>();

    public HologramService(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Hologram spawnHologram(@NotNull Location location, double lineHeight) {
        plugin.debug(0, "Creating new hologram at " + formatLocation(location) + "...");
        NativeHologram nativeHologram = new NativeHologram(plugin, this, location, lineHeight);
        this.holograms.add(nativeHologram);
        return nativeHologram;
    }

    private String formatLocation(Location location) {
        return String.format("%d, %d, %d in world %s", location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    @Override
    public void close() {
        plugin.debug(0, "Deleting all holograms...");
        this.holograms.forEach(Hologram::delete);
        this.holograms.clear();
    }
}
