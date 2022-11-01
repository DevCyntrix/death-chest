package de.helixdevs.deathchest.api;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Objects;

public interface DeathChest extends Listener, Closeable {

    default @NotNull World getWorld() {
        return Objects.requireNonNull(getLocation().getWorld());
    }

    @NotNull Location getLocation();

    @NotNull BlockState getState();

    @NotNull Inventory getInventory();

    @Nullable OfflinePlayer getPlayer();

    long getCreatedAt();

    long getExpireAt();

    boolean isExpiring();

    DeathChestSnapshot createSnapshot();


}
