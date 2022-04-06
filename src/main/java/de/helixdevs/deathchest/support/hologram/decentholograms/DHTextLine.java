package de.helixdevs.deathchest.support.hologram.decentholograms;

import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import org.jetbrains.annotations.NotNull;

public class DHTextLine implements IHologramTextLine {

    private final HologramLine line;

    public DHTextLine(HologramLine hologramLine) {
        this.line = hologramLine;
    }

    @Override
    public void rename(@NotNull String text) {
        line.setText(text);
    }

    @Override
    public void remove() {
        line.delete();
    }
}
