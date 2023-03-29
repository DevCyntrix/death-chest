package com.github.devcyntrix.deathchest.hologram;

import com.github.devcyntrix.deathchest.api.hologram.IHologram;
import com.github.devcyntrix.deathchest.api.hologram.IHologramService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class NativeHologramService implements IHologramService {

    @Override
    public @NotNull IHologram spawnHologram(@NotNull Location location) {
        return new NativeHologram(this, location);
    }
}
