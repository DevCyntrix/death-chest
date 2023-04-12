package com.github.devcyntrix.deathchest.api.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface HologramService {

    @NotNull Hologram spawnHologram(@NotNull Location location);

}
