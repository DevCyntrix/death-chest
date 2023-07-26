package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.api.animation.AnimationService;
import com.github.devcyntrix.deathchest.config.BreakAnimationOptions;
import com.github.devcyntrix.deathchest.tasks.BreakAnimationRunnable;
import com.github.devcyntrix.deathchest.util.ChestAdapter;
import com.github.devcyntrix.deathchest.util.EntityIdHelper;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BreakAnimationAdapter implements ChestAdapter {

    private final Plugin plugin;
    private final AnimationService service;
    private final BreakAnimationOptions options;

    public BreakAnimationAdapter(Plugin plugin, AnimationService service, BreakAnimationOptions options) {
        this.plugin = plugin;
        this.service = service;
        this.options = options;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        model.setBreakingEntityId(EntityIdHelper.increaseAndGet());
        BukkitTask bukkitTask = new BreakAnimationRunnable(plugin, model, service, options).runTaskTimerAsynchronously(plugin, 20, 20);
        model.getTasks().add(bukkitTask::cancel);
    }

    @Override
    public void onDestroy(DeathChestModel model) {
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
