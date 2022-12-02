package de.helixdevs.deathchest.support.hologram.cmi;

import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class CMISupportHologram implements IHologram {

    private final CMIService service;
    private final CMIHologram hologram;

    public CMISupportHologram(CMIService service, CMIHologram hologram) {
        this.service = service;
        this.hologram = hologram;
    }

    @Override
    public @NotNull IHologramService getService() {
        return service;
    }

    @Override
    public void teleport(@NotNull Location location) {
        this.hologram.setLoc(location);
        this.hologram.refresh();
    }

    @Override
    public IHologramTextLine appendLine(@NotNull String line) {
        int index = this.hologram.getLines().size(); // Get index before adding a line to reduce the effort.
        this.hologram.addLine(line);
        this.hologram.refresh();
        return new CMITextLine(index, hologram);
    }

    @Override
    public void delete() {
        this.hologram.remove();
    }
}
