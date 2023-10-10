package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestView;
import com.github.devcyntrix.deathchest.tasks.ExpirationRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ExpirationView implements ChestView {

    private final DeathChestPlugin plugin;

    public ExpirationView(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        if (!model.isExpiring())
            return;
        long untilDeletion = Math.max(0, model.getExpireAt() - System.currentTimeMillis());

        ExpirationRunnable runnable = new ExpirationRunnable(plugin, plugin.getAuditManager(), model);
        BukkitTask bukkitTask = runnable.runTaskLater(plugin, (untilDeletion / 1000) * 20);
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
