package com.github.devcyntrix.deathchest.hologram;

import com.github.devcyntrix.deathchest.api.hologram.Hologram;
import com.github.devcyntrix.deathchest.api.hologram.HologramService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class NativeHologramService implements HologramService {

    @Override
    public @NotNull Hologram spawnHologram(@NotNull Location location) {
        return new NativeHologram(this, location);
    }
}
