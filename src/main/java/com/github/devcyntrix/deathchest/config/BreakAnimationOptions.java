package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BreakAnimationOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("view-distance") double viewDistance) {

    public static final double DEFAULT_VIEW_DISTANCE = 20.0;

    @Contract("null -> new")
    public static @NotNull BreakAnimationOptions load(@Nullable ConfigurationSection section) {
        if (section == null) return new BreakAnimationOptions(false, DEFAULT_VIEW_DISTANCE);

        boolean enabled = section.getBoolean("enabled", true);
        double viewDistance = section.getDouble("view-distance", DEFAULT_VIEW_DISTANCE);
        return new BreakAnimationOptions(enabled, viewDistance);
    }

}
