package de.helixdevs.deathchest.hologram;

import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NativeHologram implements IHologram {

    private final NativeHologramService service;
    private Location location;

    private final List<NativeHologramTextLine> list = new ArrayList<>();

    public NativeHologram(NativeHologramService service, Location location) {
        this.service = service;
        this.location = location.subtract(0, 0.5, 0);
    }

    @Override
    public @NotNull IHologramService getService() {
        return service;
    }

    @Override
    public void teleport(@NotNull Location location) {
        location = location.subtract(0, 0.5, 0);

        for (NativeHologramTextLine line : list) {
            Location lL = line.getLocation().clone();
            Location diff = lL.subtract(this.location.clone());
            line.getArmorStand().teleport(location.clone().add(diff));
        }
        this.location = location;
    }

    @Override
    public IHologramTextLine appendLine(@NotNull String line) {
        NativeHologramTextLine l = new NativeHologramTextLine(location.clone().subtract(0, list.size() * 0.25, 0), line);
        list.add(l);
        return l;
    }

    @Override
    public void delete() {
        Chunk chunk = location.getChunk();
        boolean loaded = location.getChunk().isLoaded();
        if (!loaded)
            chunk.load(); // load
        this.list.forEach(NativeHologramTextLine::remove);
        this.list.clear();
        if (!loaded)
            chunk.unload(); // unload
    }
}
