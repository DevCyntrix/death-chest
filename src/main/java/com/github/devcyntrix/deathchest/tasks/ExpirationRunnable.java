package com.github.devcyntrix.deathchest.tasks;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.audit.AuditAction;
import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.AuditManager;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyChestInfo;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyReason;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.Map;

public class ExpirationRunnable extends BukkitRunnable {

    private final DeathChestPlugin plugin;
    private final AuditManager auditManager;
    private final DeathChestModel chest;

    public ExpirationRunnable(DeathChestPlugin plugin, AuditManager manager, DeathChestModel chest) {
        this.plugin = plugin;
        this.auditManager = manager;
        this.chest = chest;
    }

    @Override
    public void run() {
        // Stops the scheduler when the chest expired
        try {
            if (plugin.getDeathChestConfig().dropItemsAfterExpiration()) {
                chest.dropItems();
            }
        } catch (Exception e) {
            System.err.println("Failed to drop items of the expired death chest");
            e.printStackTrace();
        }
        if (auditManager != null)
            auditManager.audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(chest, DestroyReason.EXPIRATION, Map.of("item-drops", plugin.getDeathChestConfig().dropItemsAfterExpiration()))));
        this.plugin.getDeathChestController().destroyChest(chest);
    }
}
