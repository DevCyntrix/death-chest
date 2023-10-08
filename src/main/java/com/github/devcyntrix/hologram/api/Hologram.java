package com.github.devcyntrix.hologram.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface Hologram {

    String METADATA_KEY = "deathchest-hologram";

    @NotNull HologramService getService();

    @NotNull Location getLocation();

    void teleport(@NotNull Location location);

    HologramTextLine appendLine(@NotNull String line);

    void delete();
}
