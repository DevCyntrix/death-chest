package com.github.devcyntrix.deathchest.api.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface Hologram {

    String METADATA_KEY = "deathchest-hologram";

    @NotNull HologramService getService();

    void teleport(@NotNull Location location);

    HologramTextLine appendLine(@NotNull String line);

    void delete();
}
