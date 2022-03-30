package de.helixdevs.deathchest;

import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DeathChestHolder implements InventoryHolder {

    @NotNull
    private final Chest chest;

    public DeathChestHolder(@NotNull Chest chest) {
        this.chest = chest;
    }

    @Override
    public @NotNull Inventory getInventory() {
        //noinspection ConstantConditions
        return null;
    }

    public @NotNull Chest getChest() {
        return chest;
    }
}
