package com.github.devcyntrix.deathchest.api.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface IHologramService {

    @NotNull IHologram spawnHologram(@NotNull Location location);

}
