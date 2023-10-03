package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.util.ChestView;
import org.bukkit.entity.HumanEntity;

import java.util.ArrayList;
import java.util.List;

public class CloseInventoryView implements ChestView {
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
        onCreate(model);
    }

    @Override
    public void onUnload(DeathChestModel model) {
        onDestroy(model);
    }
}
