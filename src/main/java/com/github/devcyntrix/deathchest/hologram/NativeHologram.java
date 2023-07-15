package com.github.devcyntrix.deathchest.hologram;

import com.github.devcyntrix.deathchest.api.hologram.Hologram;
import com.github.devcyntrix.deathchest.api.hologram.HologramService;
import com.github.devcyntrix.deathchest.api.hologram.HologramTextLine;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NativeHologram implements Hologram {

    private final HologramService service;
    private Location location;

    private final List<NativeHologramTextLine> list = new ArrayList<>();

    public NativeHologram(HologramService service, Location location) {
        this.service = service;
        this.location = location.subtract(0, 0.5, 0);
    }

    @Override
    public @NotNull HologramService getService() {
        return service;
    }

    @Override
    public void teleport(@NotNull Location location) {
        location = location.subtract(0, 0.5, 0);

        for (NativeHologramTextLine line : list) {
            Location lL = line.getLocation().clone();
            Location diff = lL.subtract(this.location.clone());
            ArmorStand armorStand = line.getArmorStand();
            if (armorStand == null)
                continue;
            armorStand.teleport(location.clone().add(diff));
        }
        this.location = location;
    }

    @Override
    public HologramTextLine appendLine(@NotNull String line) {
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
