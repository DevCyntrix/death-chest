package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.deathchest.api.DeathChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A holder for the inventory to get the chest through the inventory
 */
public class DeathChestHolder implements InventoryHolder {

    @NotNull
    private final DeathChest chest;

    public DeathChestHolder(@NotNull DeathChest chest) {
        this.chest = chest;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return chest.getInventory();
    }

    public @NotNull DeathChest getChest() {
        return chest;
    }
}
