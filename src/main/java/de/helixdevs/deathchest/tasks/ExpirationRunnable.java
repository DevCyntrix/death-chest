package de.helixdevs.deathchest.tasks;

import de.helixdevs.deathchest.api.DeathChest;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class ExpirationRunnable extends BukkitRunnable {

    private final DeathChest chest;

    public ExpirationRunnable(DeathChest chest) {
        this.chest = chest;
    }

    @Override
    public void run() {
        // Stops the scheduler when the chest expired
        if (chest.isExpiring() && !chest.isClosed()) {
            long duration = chest.getExpireAt() - System.currentTimeMillis();
            if (duration > 0)
                return;
            Bukkit.getScheduler().runTask(chest.getPlugin(), () -> {
                try {
                    if (chest.getConfig().dropItemsAfterExpiration()) {
                        chest.dropItems();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to drop items of the expired death chest");
                    e.printStackTrace();
                }
                try {
                    chest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            cancel(); // Cancel task after executing the next task

        }
    }
}
