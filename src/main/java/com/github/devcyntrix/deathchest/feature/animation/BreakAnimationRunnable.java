package com.github.devcyntrix.deathchest.feature.animation;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.BreakAnimationOptions;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.stream.Stream;

public class BreakAnimationRunnable extends BukkitRunnable {

    private final DeathChestPlugin plugin;
    private final DeathChestModel chest;
    private final AnimationService animationService;
    private final BreakAnimationOptions options;

    public BreakAnimationRunnable(DeathChestPlugin plugin, DeathChestModel chest, AnimationService animationService, BreakAnimationOptions options) {
        this.plugin = plugin;
        this.chest = chest;
        this.animationService = animationService;
        this.options = options;
    }

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

        // 修复 (fix3): 箱子过期后动画任务仍可能在跑（玩家挖箱过程中过期），
        // process 会 >1.0 → state >9 → progress 非法 → sendBlockDamage 抛异常（MCSM 线上 2026-08-12 实测卡服事件）
        double process = Math.min(1.0, Math.max(0.0, (double) (System.currentTimeMillis() - chest.getCreatedAt()) / (chest.getExpireAt() - chest.getCreatedAt())));

        try {
            if (!DeathChestPlugin.isTest()) {
                Stream<Player> playerStream = Bukkit.getScheduler().callSyncMethod(plugin, () -> world.getNearbyEntities(chest.getLocation(), options.viewDistance(), options.viewDistance(), options.viewDistance(), entity -> entity.getType() == EntityType.PLAYER).stream().map(entity -> (Player) entity)).get(1, TimeUnit.SECONDS);
                animationService.spawnBlockBreakAnimation(entityId, chest.getLocation().toVector(), (int) (9 * process), playerStream);
            }
        } catch (ExecutionException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to spawn block break animation", e);
        } catch (TimeoutException e) {
            if (plugin.isDebugMode()) {
                plugin.getLogger().warning("Warning: Getting nearby entities took longer than 1 second.");
            }
        } catch (InterruptedException e) {
            cancel();
        }
    }
}
