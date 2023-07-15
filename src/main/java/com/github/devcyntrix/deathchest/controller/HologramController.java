package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.api.hologram.Hologram;
import com.github.devcyntrix.deathchest.api.hologram.HologramService;
import com.github.devcyntrix.deathchest.hologram.NativeHologram;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class HologramController implements HologramService, Closeable {

    private final Set<Hologram> holograms = new HashSet<>();

    @Override
    public @NotNull Hologram spawnHologram(@NotNull Location location) {
        NativeHologram nativeHologram = new NativeHologram(this, location);
        this.holograms.add(nativeHologram);
        return nativeHologram;
    }

    @Override
    public void close() {
        this.holograms.forEach(Hologram::delete);
        this.holograms.clear();
    }
}
