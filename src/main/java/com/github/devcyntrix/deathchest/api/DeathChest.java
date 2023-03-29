package com.github.devcyntrix.deathchest.api;

import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Objects;
import java.util.logging.Logger;

public interface DeathChest extends Listener, Closeable {

    @NotNull Plugin getPlugin();

    @NotNull DeathChestConfig getConfig();

    default @NotNull Logger getLogger() {
        return getPlugin().getLogger();
    }

    default @NotNull World getWorld() {
        return Objects.requireNonNull(getLocation().getWorld());
    }

    @NotNull Location getLocation();

    @NotNull BlockState getState();

    @NotNull Inventory getInventory();

    @Nullable OfflinePlayer getPlayer();

    default void dropItems() {
        dropItems(getLocation());
    }

    void dropItems(@NotNull Location location);

    /**
     * Gets the unix time when the chest was created
     *
     * @return the unix time
     */
    long getCreatedAt();

    /**
     * Gets the unix time when the chest will disappear
     *
     * @return the unix time
     */
    long getExpireAt();

    /**
     * Gets weather the chest will expire.
     *
     * @return true if the expiration time is set to a larger number than 0
     */
    boolean isExpiring();

    /**
     * Gets weather the chest is protected against thieves.
     *
     * @return true if the player dies and has a certain permission.
     */
    boolean isProtected();

    /**
     * Creates a snapshot of the chest which can be saved.
     *
     * @return a snapshot of the chest
     */
    DeathChestSnapshot createSnapshot();

    boolean isClosed();

}
