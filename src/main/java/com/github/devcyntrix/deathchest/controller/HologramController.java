package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.hologram.NativeHologram;
import com.github.devcyntrix.hologram.api.Hologram;
import com.github.devcyntrix.hologram.api.HologramService;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;

public class HologramController implements HologramService, Closeable {

    private final JavaPlugin plugin;
    private final Set<Hologram> holograms = new HashSet<>();

    public HologramController(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Hologram spawnHologram(@NotNull Location location) {
        NativeHologram nativeHologram = new NativeHologram(plugin, this, location);
        this.holograms.add(nativeHologram);
        return nativeHologram;
    }

    @Override
    public void close() {
        this.holograms.forEach(Hologram::delete);
        this.holograms.clear();
    }
}
