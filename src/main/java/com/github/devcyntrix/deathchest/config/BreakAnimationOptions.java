package com.github.devcyntrix.deathchest.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BreakAnimationOptions(boolean enabled, double viewDistance) {

    public static @NotNull BreakAnimationOptions load(@Nullable ConfigurationSection section) {
        if (section == null) return new BreakAnimationOptions(false, 0.0);

        boolean enabled = section.getBoolean("enabled", true);
        double viewDistance = section.getDouble("view-distance", 20.0);
        return new BreakAnimationOptions(enabled, viewDistance);
    }

}
