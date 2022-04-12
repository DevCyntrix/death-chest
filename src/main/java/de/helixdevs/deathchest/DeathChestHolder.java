package de.helixdevs.deathchest;

import de.helixdevs.deathchest.api.DeathChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DeathChestHolder implements InventoryHolder {

    @NotNull
    private final DeathChest chest;

    public DeathChestHolder(@NotNull DeathChest chest) {
        this.chest = chest;
    }

    @Override
    public @NotNull Inventory getInventory() {
        //noinspection ConstantConditions
        return null;
    }

    public @NotNull DeathChest getChest() {
        return chest;
    }
}
