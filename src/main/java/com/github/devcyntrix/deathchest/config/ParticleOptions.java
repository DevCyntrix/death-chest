package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ParticleOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("radius") double radius,
        @SerializedName("count") int count,
        @SerializedName("speed") double speed) {

    public static final double DEFAULT_RADIUS = 1.0;
    public static final int DEFAULT_COUNT = 32;
    public static final double DEFAULT_SPEED = 20;

    @Contract("null -> new")
    public static @NotNull ParticleOptions load(@Nullable ConfigurationSection section) {
        if (section == null) return new ParticleOptions(false, DEFAULT_RADIUS, DEFAULT_COUNT, DEFAULT_SPEED);

        boolean enabled = section.getBoolean("enabled", true);
        double radius = section.getDouble("radius", DEFAULT_RADIUS);
        int count = section.getInt("count", DEFAULT_COUNT);
        double speed = section.getDouble("speed", DEFAULT_SPEED);

        return new ParticleOptions(enabled, radius, count, speed);
    }

}
