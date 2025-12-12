package com.github.devcyntrix.deathchest.util;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public final class ParticleUtils {

    public static Particle findParticle(@NotNull String name, @NotNull String alternative) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Particle.valueOf(alternative);
        }
    }

}
