package com.github.devcyntrix.deathchest.tasks;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public class ParticleRunnable extends BukkitRunnable {

    private final Location center;
    private final int segments;
    private final double radius;

    private final Consumer<Location> consumer;

    public ParticleRunnable(Location center, int segments, double radius, Consumer<Location> consumer) {
        this.center = center;
        this.segments = segments;
        this.radius = radius;
        this.consumer = consumer;
    }

    private int counter = 0;

    @Override
    public void run() {
        if (counter > 16)
            counter = 0;

        double x = Math.cos(4 * Math.PI * 1 / segments * counter) * radius;
        double z = Math.sin(4 * Math.PI * 1 / segments * counter) * radius;

        Location clone = center.clone().add(x, 0, z);
        consumer.accept(clone);

        counter++;
    }
}
