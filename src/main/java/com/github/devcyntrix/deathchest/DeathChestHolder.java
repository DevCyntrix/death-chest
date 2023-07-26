package com.github.devcyntrix.deathchest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A holder for the inventory to get the chest through the inventory
 */
@AllArgsConstructor
@Getter
public class DeathChestHolder implements InventoryHolder {

    private final DeathChestModel model;

    @NotNull
    @Override
    public Inventory getInventory() {
        return null;
    }
}
