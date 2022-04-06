package de.helixdevs.deathchest.support.hologram.holograms;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.TextLine;
import com.sainttx.holograms.api.line.TextualHologramLine;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class HologramsHologram implements IHologram {

    private final HologramsService service;
    private final Hologram hologram;

    public HologramsHologram(HologramsService service, Hologram hologram) {
        this.service = service;
        this.hologram = hologram;
    }

    @Override
    public @NotNull IHologramService getService() {
        return service;
    }

    @Override
    public void teleport(@NotNull Location location) {
        hologram.teleport(location);
    }

    @Override
    public IHologramTextLine appendLine(@NotNull String line) {
        TextualHologramLine hologramLine = new TextLine(this.hologram, line);
        return new HologramsTextLine(this.hologram, hologramLine);
    }

    @Override
    public void delete() {
        hologram.despawn();
        service.getHologramManager().deleteHologram(hologram);
    }
}
