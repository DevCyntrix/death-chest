package de.helixdevs.deathchest.support.hologram.holographicdisplays;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class HDHologram implements IHologram {

    private final HDService service;
    private final Hologram hologram;

    public HDHologram(HDService service, Hologram hologram) {
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
        TextLine textLine = hologram.appendTextLine(line);
        return new HDTextLine(textLine);
    }

    @Override
    public void delete() {
        hologram.delete();
    }
}
