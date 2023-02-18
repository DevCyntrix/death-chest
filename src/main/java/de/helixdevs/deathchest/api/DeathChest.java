package de.helixdevs.deathchest.api;

import de.helixdevs.deathchest.config.DeathChestConfig;
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

    long getCreatedAt();

    long getExpireAt();

    boolean isExpiring();

    boolean isProtected();

    DeathChestSnapshot createSnapshot();

    boolean isClosed();

}
