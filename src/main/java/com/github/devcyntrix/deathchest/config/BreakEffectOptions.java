package com.github.devcyntrix.deathchest.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BreakEffectOptions(boolean enabled, double viewDistance) {

    public static @NotNull BreakEffectOptions load(@Nullable ConfigurationSection section) {
        if (section == null) return new BreakEffectOptions(false, 0.0);

        boolean enabled = section.getBoolean("enabled", true);
        double viewDistance = section.getDouble("view-distance", 20.0);
        return new BreakEffectOptions(enabled, viewDistance);
    }

}
