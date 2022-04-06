package de.helixdevs.deathchest.support.hologram.holograms;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.TextualHologramLine;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.jetbrains.annotations.NotNull;

public class HologramsTextLine implements IHologramTextLine {

    private final Hologram hologram;
    private final TextualHologramLine hologramLine;

    public HologramsTextLine(Hologram hologram, TextualHologramLine hologramLine) {
        this.hologram = hologram;
        this.hologramLine = hologramLine;
    }

    @Override
    public void rename(@NotNull String text) {
        hologramLine.setText(text);
    }

    @Override
    public void remove() {
        hologram.removeLine(hologramLine);
    }
}
