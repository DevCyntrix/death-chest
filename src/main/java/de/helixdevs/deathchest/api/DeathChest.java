package de.helixdevs.deathchest.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Objects;

public interface DeathChest extends Listener, Closeable {

    default @NotNull World getWorld() {
        return Objects.requireNonNull(getLocation().getWorld());
    }

    @NotNull Location getLocation();

    Chest getBukkitChest();

    Inventory getInventory();

    long getCreatedAt();

    long getExpireAt();

    boolean isExpiring();

}
