package de.helixdevs.deathchest.support.hologram.cmi;

import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.jetbrains.annotations.NotNull;

public class CMITextLine implements IHologramTextLine {

    private final int index;
    private final CMIHologram hologram;

    public CMITextLine(int index, CMIHologram hologram) {
        this.index = index;
        this.hologram = hologram;
    }

    @Override
    public void rename(@NotNull String text) {
        hologram.setLine(index, text);
        hologram.refresh();
    }

    @Override
    public void remove() {
        hologram.getLines().remove(index);
    }
}
