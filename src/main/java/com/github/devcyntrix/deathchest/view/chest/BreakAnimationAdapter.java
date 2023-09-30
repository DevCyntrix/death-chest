package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import com.github.devcyntrix.deathchest.config.BreakAnimationOptions;
import com.github.devcyntrix.deathchest.tasks.BreakAnimationRunnable;
import com.github.devcyntrix.deathchest.util.ChestAdapter;
import com.github.devcyntrix.deathchest.util.EntityIdHelper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class BreakAnimationAdapter implements ChestAdapter {

    @NotNull
    private final DeathChestPlugin plugin;
    @Nullable
    private final BreakAnimationService service;
    @NotNull
    private final BreakAnimationOptions options;

    public BreakAnimationAdapter(@NotNull DeathChestPlugin plugin, @Nullable BreakAnimationService service, @NotNull BreakAnimationOptions options) {
        this.plugin = plugin;
        this.service = service;
        this.options = options;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        if(service != null) {
            model.setBreakingEntityId(EntityIdHelper.increaseAndGet());
            plugin.debug(0, "Starting block break animation using entity id %d".formatted(model.getBreakingEntityId()));
            BukkitTask bukkitTask = new BreakAnimationRunnable(plugin, model, service, options).runTaskTimerAsynchronously(plugin, 20, 20);
            model.getTasks().add(bukkitTask::cancel);
        }
    }

    @Override
    public void onDestroy(DeathChestModel model) {
        Integer breakingId = model.getBreakingEntityId();
        if (breakingId == null)
            return;

        try {
            // Resets the breaking animation if the service is available
            if (model.isExpiring()) {
                Stream<Player> playerStream = model.getWorld().getNearbyEntities(model.getLocation(), 20, 20, 20, entity -> entity.getType() == EntityType.PLAYER).stream().map(entity -> (Player) entity);
                service.spawnBlockBreakAnimation(breakingId, model.getLocation().toVector(), -1, playerStream);
            }
        } catch (Exception e) {
            System.err.println("Failed to reset the block animation of all players in the area");
            e.printStackTrace();
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
