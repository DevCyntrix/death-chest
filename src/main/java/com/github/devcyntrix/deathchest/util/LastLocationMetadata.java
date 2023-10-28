package com.github.devcyntrix.deathchest.util;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class LastLocationMetadata extends MetadataValueAdapter {

    private Location location;
    private long updatedAt;

    /**
     * Initializes a FixedMetadataValue with an Object
     *
     * @param owningPlugin the {@link Plugin} that created this metadata value
     * @param value        the value assigned to this metadata value
     */
    public LastLocationMetadata(@NotNull Plugin owningPlugin, @NotNull Location location) {
        super(owningPlugin);
        this.location = location;
        this.updatedAt = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public Location value() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public void invalidate() {

    }
}
