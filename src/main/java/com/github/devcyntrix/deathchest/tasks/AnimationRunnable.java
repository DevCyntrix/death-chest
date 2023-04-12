package com.github.devcyntrix.deathchest.tasks;

import com.github.devcyntrix.deathchest.api.DeathChest;
import com.github.devcyntrix.deathchest.api.animation.AnimationService;
import com.github.devcyntrix.deathchest.config.BreakEffectOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class AnimationRunnable extends BukkitRunnable {

    private final DeathChest chest;
    private final AnimationService animationService;
    private final BreakEffectOptions options;
    private final int breakingEntityId;

    public AnimationRunnable(DeathChest chest, AnimationService animationService, BreakEffectOptions options, int breakingEntityId) {
        this.chest = chest;
        this.animationService = animationService;
        this.options = options;
        this.breakingEntityId = breakingEntityId;
    }

    @Override
    public void run() {
        double process = (double) (System.currentTimeMillis() - chest.getCreatedAt()) / (chest.getExpireAt() - chest.getCreatedAt());

        try {
            Stream<Player> playerStream = Bukkit.getScheduler().callSyncMethod(chest.getPlugin(), () -> chest.getWorld().getNearbyEntities(chest.getLocation(), options.viewDistance(), options.viewDistance(), options.viewDistance(), entity -> entity.getType() == EntityType.PLAYER).stream().map(entity -> (Player) entity)).get(1, TimeUnit.SECONDS);
            animationService.spawnBlockBreakAnimation(breakingEntityId, chest.getLocation().toVector(), (int) (9 * process), playerStream);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            chest.getLogger().warning("Warning get nearby entities takes longer than 1 second.");
        } catch (InterruptedException e) {
            cancel();
        }
    }
}
