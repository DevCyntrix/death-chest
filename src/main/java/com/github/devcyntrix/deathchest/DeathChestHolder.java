package com.github.devcyntrix.deathchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A holder for the inventory to get the chest through the inventory
 */
public class DeathChestHolder implements InventoryHolder {

    public DeathChestHolder() {
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return null;
    }
}
