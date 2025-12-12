package com.github.devcyntrix.deathchest.feature.animation;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.BreakAnimationOptions;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

@AllArgsConstructor
public class BreakAnimationRunnable extends BukkitRunnable {

    private final DeathChestPlugin plugin;
    private final DeathChestModel chest;
    private final AnimationService animationService;
    private final BreakAnimationOptions options;

    @Override
    public void run() {
        Integer entityId = chest.getBreakingEntityId();
        if (entityId == null) {
            cancel();
            return;
        }

        World world = chest.getWorld();
        if (world == null) {
            cancel();
            return;
        }

        double process = (double) (System.currentTimeMillis() - chest.getCreatedAt()) / (chest.getExpireAt() - chest.getCreatedAt());

        try {
            if(!plugin.isTest()) {
                Stream<Player> playerStream = Bukkit.getScheduler().callSyncMethod(plugin, () -> world.getNearbyEntities(chest.getLocation(), options.viewDistance(), options.viewDistance(), options.viewDistance(), entity -> entity.getType() == EntityType.PLAYER).stream().map(entity -> (Player) entity)).get(1, TimeUnit.SECONDS);
                animationService.spawnBlockBreakAnimation(entityId, chest.getLocation().toVector(), (int) (9 * process), playerStream);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            if (plugin.isDebugMode()) {
                plugin.getLogger().warning("Warning: Getting nearby entities took longer than 1 second.");
            }
        } catch (InterruptedException e) {
            cancel();
        }
    }
}
