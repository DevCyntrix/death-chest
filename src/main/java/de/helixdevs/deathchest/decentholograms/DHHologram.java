package de.helixdevs.deathchest.decentholograms;

import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class DHHologram implements IHologram {

    private final DHService service;
    private final Hologram hologram;

    public DHHologram(DHService service, Hologram hologram) {
        this.service = service;
        this.hologram = hologram;
    }

    @Override
    public @NotNull IHologramService getService() {
        return service;
    }

    @Override
    public void teleport(@NotNull Location location) {
        this.hologram.setLocation(location);
    }

    @Override
    public IHologramTextLine appendLine(@NotNull String line) {
        HologramLine hologramLine = DHAPI.addHologramLine(hologram, line);
        return new DHTextLine(hologramLine);
    }

    @Override
    public void delete() {
        this.hologram.delete();
    }
}
