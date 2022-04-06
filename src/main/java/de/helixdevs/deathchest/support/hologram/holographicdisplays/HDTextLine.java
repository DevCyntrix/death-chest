package de.helixdevs.deathchest.support.hologram.holographicdisplays;

import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.jetbrains.annotations.NotNull;

public class HDTextLine implements IHologramTextLine {

    private final TextLine line;

    public HDTextLine(TextLine line) {
        this.line = line;
    }

    @Override
    public void rename(@NotNull String text) {
        line.setText(text);
    }

    @Override
    public void remove() {
        line.removeLine();
    }
}
