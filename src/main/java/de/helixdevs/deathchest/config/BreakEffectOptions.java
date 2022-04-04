package de.helixdevs.deathchest.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public record BreakEffectOptions(boolean enabled, double viewDistance) {

    public static @Nullable BreakEffectOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled");
        double viewDistance = section.getDouble("view-distance");
        return new BreakEffectOptions(enabled, viewDistance);
    }

}
