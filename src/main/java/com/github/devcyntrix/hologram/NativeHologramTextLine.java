package com.github.devcyntrix.hologram;

import com.github.devcyntrix.hologram.api.Hologram;
import com.github.devcyntrix.hologram.api.HologramTextLine;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NativeHologramTextLine implements HologramTextLine {

    private final Location location;
    private final UUID armorStand;

    public NativeHologramTextLine(@NotNull Plugin plugin, @NotNull Location location, @NotNull String text) {
        Preconditions.checkNotNull(location.getWorld());
        this.location = location;
        this.armorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setMarker(true);
            stand.setInvisible(true);
            stand.setCustomName(text);
            stand.setCustomNameVisible(true);
            stand.setMetadata(Hologram.METADATA_KEY, new FixedMetadataValue(plugin, true));
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
