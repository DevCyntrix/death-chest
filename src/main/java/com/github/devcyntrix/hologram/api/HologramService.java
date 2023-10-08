package com.github.devcyntrix.hologram.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface HologramService {

    @NotNull Hologram spawnHologram(@NotNull Location location, double lineHeight);

}
