package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestView;
import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import com.github.devcyntrix.deathchest.config.BreakAnimationOptions;
import com.github.devcyntrix.deathchest.tasks.BreakAnimationRunnable;
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
    private final BreakAnimationService service;
    @NotNull
    private final BreakAnimationOptions options;

    public BreakAnimationView(@NotNull DeathChestPlugin plugin, @Nullable BreakAnimationService service, @NotNull BreakAnimationOptions options) {
        this.plugin = plugin;
        this.service = service;
        this.options = options;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        if (service == null)
            return;

        model.setBreakingEntityId(EntityIdHelper.increaseAndGet());
        plugin.debug(0, "Starting block break animation using entity id %d".formatted(model.getBreakingEntityId()));
        BukkitTask bukkitTask = new BreakAnimationRunnable(plugin, model, service, options).runTaskTimerAsynchronously(plugin, 20, 20);
        model.getTasks().add(bukkitTask::cancel);
    }

    @Override
    public void onDestroy(DeathChestModel model) {
        if (service == null)
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
                    service.spawnBlockBreakAnimation(breakingId, model.getLocation().toVector(), -1, playerStream);
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
