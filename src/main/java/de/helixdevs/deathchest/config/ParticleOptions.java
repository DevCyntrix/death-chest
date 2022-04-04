package de.helixdevs.deathchest.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public record ParticleOptions(boolean enabled, double radius, int count, double speed) {

    public static @Nullable ParticleOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled");
        double radius = section.getDouble("radius");
        int count = section.getInt("count");
        double speed = section.getDouble("speed");

        return new ParticleOptions(enabled, radius, count, speed);
    }

}
