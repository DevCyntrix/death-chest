package de.helixdevs.deathchest.tasks;

import de.helixdevs.deathchest.api.DeathChest;
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
    }
}
