package com.github.devcyntrix.hologram;

import com.github.devcyntrix.hologram.api.Hologram;
import com.github.devcyntrix.hologram.api.HologramService;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NativeHologram implements Hologram {

    @NotNull
    private final JavaPlugin plugin;
    @NotNull
    @Getter
    private final HologramService service;
    private final double lineHeight;
    @NotNull
    private Location location;

    private final List<NativeHologramTextLine> list = new ArrayList<>();

    public NativeHologram(@NotNull JavaPlugin plugin, @NotNull HologramService service, @NotNull Location location, double lineHeight) {
        Preconditions.checkNotNull(plugin);
        Preconditions.checkNotNull(service);
        Preconditions.checkNotNull(location);

        this.plugin = plugin;
        this.service = service;
        this.location = location;
        this.lineHeight = lineHeight;
    }

    @NotNull
    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void teleport(@NotNull Location location) {

        for (NativeHologramTextLine line : list) {
            Location oldPos = line.getLocation().clone();
            Location diff = oldPos.subtract(this.location.clone());
            line.teleport(location.clone().add(diff));
        }
        this.location = location;
    }

    @Override
    public HologramTextLine appendLine(@NotNull String line) {

        list.forEach(lineRef -> lineRef.teleport(lineRef.getLocation().add(0, lineHeight, 0)));

        NativeHologramTextLine l = new NativeHologramTextLine(this.plugin, location.clone(), line);
        list.add(l);
        return l;
    }

    @Override
    public void delete() {
        this.list.forEach(NativeHologramTextLine::remove);
        this.list.clear();
    }
}
