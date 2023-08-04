package com.github.devcyntrix.deathchest.tasks;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import com.github.devcyntrix.deathchest.config.BreakAnimationOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class BreakAnimationRunnable extends BukkitRunnable {

    private final Plugin plugin;
    private final DeathChestModel chest;
    private final BreakAnimationService breakAnimationService;
    private final BreakAnimationOptions options;

    public BreakAnimationRunnable(Plugin plugin, DeathChestModel chest, BreakAnimationService breakAnimationService, BreakAnimationOptions options) {
        this.plugin = plugin;
        this.chest = chest;
        this.breakAnimationService = breakAnimationService;
        this.options = options;
    }

    @Override
    public void run() {
        Integer entityId = chest.getBreakingEntityId();
        if (entityId == null) {
            cancel();
            return;
        }

        double process = (double) (System.currentTimeMillis() - chest.getCreatedAt()) / (chest.getExpireAt() - chest.getCreatedAt());

        try {
            Stream<Player> playerStream = Bukkit.getScheduler().callSyncMethod(plugin, () -> chest.getWorld().getNearbyEntities(chest.getLocation(), options.viewDistance(), options.viewDistance(), options.viewDistance(), entity -> entity.getType() == EntityType.PLAYER).stream().map(entity -> (Player) entity)).get(1, TimeUnit.SECONDS);
            breakAnimationService.spawnBlockBreakAnimation(entityId, chest.getLocation().toVector(), (int) (9 * process), playerStream);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("Warning: Getting nearby entities took longer than 1 second.");
        } catch (InterruptedException e) {
            cancel();
        }
    }
}
