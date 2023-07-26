package com.github.devcyntrix.hologram;

import com.github.devcyntrix.hologram.api.Hologram;
import com.github.devcyntrix.hologram.api.HologramService;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NativeHologram implements Hologram {

    @NotNull
    private final JavaPlugin plugin;
    @NotNull
    private final HologramService service;
    @NotNull
    private Location location;

    private final List<NativeHologramTextLine> list = new ArrayList<>();

    public NativeHologram(@NotNull JavaPlugin plugin, @NotNull HologramService service, Location location) {
        Preconditions.checkNotNull(plugin);
        Preconditions.checkNotNull(service);
        Preconditions.checkNotNull(location);

        this.plugin = plugin;
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
        NativeHologramTextLine l = new NativeHologramTextLine(this.plugin, location.clone().subtract(0, list.size() * 0.25, 0), line);
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
