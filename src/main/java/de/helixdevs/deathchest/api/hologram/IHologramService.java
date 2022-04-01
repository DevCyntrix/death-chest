package de.helixdevs.deathchest.api.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface IHologramService {

    boolean isEnabled();

    @NotNull IHologram spawnHologram(@NotNull Location location);

}
