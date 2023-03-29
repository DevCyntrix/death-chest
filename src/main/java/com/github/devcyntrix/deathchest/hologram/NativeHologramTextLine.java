package com.github.devcyntrix.deathchest.hologram;

import com.github.devcyntrix.deathchest.api.hologram.IHologramTextLine;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NativeHologramTextLine implements IHologramTextLine {

    private final Location location;
    private final UUID armorStand;

    public NativeHologramTextLine(@NotNull Location location, @NotNull String text) {
        Preconditions.checkNotNull(location.getWorld());
        this.location = location;
        this.armorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setMarker(true);
            stand.setInvisible(true);
            stand.setCustomName(text);
            stand.setCustomNameVisible(true);
        }).getUniqueId();
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public @Nullable ArmorStand getArmorStand() {
        return (ArmorStand) Bukkit.getEntity(armorStand);
    }

    @Override
    public void rename(@NotNull String text) {
        ArmorStand stand = getArmorStand();
        if (stand == null || stand.isDead())
            return;
        stand.setCustomName(text);
    }

    @Override
    public void remove() {
        ArmorStand stand = getArmorStand();
        if (stand == null)
            return;
        stand.remove();
    }
}
