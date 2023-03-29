package com.github.devcyntrix.deathchest.tasks;

import com.github.devcyntrix.deathchest.api.DeathChest;
import com.github.devcyntrix.deathchest.api.audit.AuditAction;
import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.AuditManager;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyChestInfo;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyReason;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class ExpirationRunnable extends BukkitRunnable {

    private final AuditManager auditManager;
    private final DeathChest chest;

    public ExpirationRunnable(AuditManager manager, DeathChest chest) {
        this.auditManager = manager;
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
            if (auditManager != null)
                auditManager.audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(chest, DestroyReason.EXPIRATION, Map.of("item-drops", chest.getConfig().dropItemsAfterExpiration()))));
            chest.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
