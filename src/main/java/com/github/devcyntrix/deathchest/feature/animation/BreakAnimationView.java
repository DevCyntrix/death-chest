package com.github.devcyntrix.deathchest.feature.animation;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestView;
import com.github.devcyntrix.deathchest.config.BreakAnimationOptions;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import com.github.devcyntrix.deathchest.util.EntityIdHelper;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.stream.Stream;

public class BreakAnimationView implements ChestView {

    @NotNull
    private final DeathChestPlugin plugin;
    @Nullable
    private final AnimationService animationService;
    @NotNull
    private final BreakAnimationOptions options;

    public BreakAnimationView(@NotNull DeathChestPlugin plugin, @Nullable AnimationService animationService, @NotNull BreakAnimationOptions options) {
        this.plugin = plugin;
        this.animationService = animationService;
        this.options = options;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        if (animationService == null)
            return;

        model.setBreakingEntityId(EntityIdHelper.increaseAndGet());
        plugin.debug(0, "Starting block break animation using entity id %d".formatted(model.getBreakingEntityId()));
        BukkitTask bukkitTask = new BreakAnimationRunnable(plugin, model, animationService, options).runTaskTimerAsynchronously(plugin, 20, 20);
        model.getTasks().add(bukkitTask::cancel);
    }

    @Override
    public void onDestroy(DeathChestModel model) {
        if (animationService == null)
            return;

        Integer breakingId = model.getBreakingEntityId();
        if (breakingId == null)
            return;

        try {
            // Resets the breaking animation if the service is available
            if (model.isExpiring()) {
                World world = model.getWorld();
                if (world != null) {
                    Stream<Player> playerStream = model.getWorld().getNearbyEntities(model.getLocation(), 20, 20, 20, entity -> entity.getType() == EntityType.PLAYER).stream().map(entity -> (Player) entity);
                    animationService.spawnBlockBreakAnimation(breakingId, model.getLocation().toVector(), -1, playerStream);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to reset the block animation of all players in the area", e);
        }


        model.setBreakingEntityId(null);
    }

    @Override
    public void onLoad(DeathChestModel model) {
        onCreate(model);
    }

    @Override
    public void onUnload(DeathChestModel model) {
        onDestroy(model);
    }
}
