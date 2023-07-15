package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.util.ChestListener;
import org.bukkit.entity.HumanEntity;

import java.util.ArrayList;
import java.util.List;

public class CloseInventoryListener implements ChestListener {
    @Override
    public void onCreate(DeathChestModel model) {

    }

    @Override
    public void onDestroy(DeathChestModel model) {
        if (model.getInventory() == null)
            return;

        try {
            List<HumanEntity> humanEntities = new ArrayList<>(model.getInventory().getViewers()); // Copies the list to avoid a concurrent modification exception
            humanEntities.forEach(HumanEntity::closeInventory);
        } catch (Exception e) {
            System.err.println("Failed to close inventories of viewers.");
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad(DeathChestModel model) {

    }

    @Override
    public void onUnload(DeathChestModel model) {
        onDestroy(model);
    }
}
