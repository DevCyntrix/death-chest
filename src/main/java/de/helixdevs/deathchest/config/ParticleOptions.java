package de.helixdevs.deathchest.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public record ParticleOptions(boolean enabled, double radius, int count, double speed) {

    public static @Nullable ParticleOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled", true);
        double radius = section.getDouble("radius", 1.0);
        int count = section.getInt("count", 32);
        double speed = section.getDouble("speed", 20.0);

        return new ParticleOptions(enabled, radius, count, speed);
    }

}
