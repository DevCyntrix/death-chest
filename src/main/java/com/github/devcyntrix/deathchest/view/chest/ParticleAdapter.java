package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.config.ParticleOptions;
import com.github.devcyntrix.deathchest.tasks.ParticleRunnable;
import com.github.devcyntrix.deathchest.util.ChestAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class ParticleAdapter implements ChestAdapter {

    private final Plugin plugin;
    private final ParticleOptions options;

    public ParticleAdapter(Plugin plugin, ParticleOptions options) {
        this.plugin = plugin;
        this.options = options;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        Particle.DustOptions orangeDustOptions = new Particle.DustOptions(Color.ORANGE, 0.75f);
        Particle.DustOptions aquaDustOptions = new Particle.DustOptions(Color.AQUA, 0.75f);

        BukkitTask bukkitTask = new ParticleRunnable(model.getLocation(), options.count(), options.radius(), particleLocation -> {
            // Orange dust
            Location orangeDust = particleLocation.clone().add(0.5, 0.5, 0.5); // Center the particle location
            Bukkit.getScheduler().runTask(plugin, () -> model.getWorld().spawnParticle(Particle.REDSTONE, orangeDust, 1, orangeDustOptions));

            // Aqua dust
            Location aquaDust = orangeDust.clone().subtract(0, 0.1, 0);
            Bukkit.getScheduler().runTask(plugin, () -> model.getWorld().spawnParticle(Particle.REDSTONE, aquaDust, 1, aquaDustOptions));

        }).runTaskTimerAsynchronously(this.plugin, 0, (long) (20 / options.speed()));
        model.getTasks().add(bukkitTask::cancel);
    }

    @Override
    public void onDestroy(DeathChestModel model) {

    }

    @Override
    public void onLoad(DeathChestModel model) {
        onCreate(model);
    }

    @Override
    public void onUnload(DeathChestModel model) {

    }
}
