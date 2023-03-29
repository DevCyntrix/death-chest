package com.github.devcyntrix.deathchest.api.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface IHologram {

    @NotNull IHologramService getService();

    void teleport(@NotNull Location location);

    IHologramTextLine appendLine(@NotNull String line);

    void delete();
}
