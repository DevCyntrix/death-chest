package de.helixdevs.deathchest.hologram;

import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class NativeHologramService implements IHologramService {

    @Override
    public @NotNull IHologram spawnHologram(@NotNull Location location) {
        return new NativeHologram(this, location);
    }
}
