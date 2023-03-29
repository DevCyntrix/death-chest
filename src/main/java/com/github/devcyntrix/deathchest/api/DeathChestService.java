package com.github.devcyntrix.deathchest.api;

import com.github.devcyntrix.deathchest.api.animation.IAnimationService;
import com.github.devcyntrix.deathchest.api.hologram.IHologramService;
import com.github.devcyntrix.deathchest.api.protection.IProtectionService;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;

public interface DeathChestService {

    boolean canPlaceChestAt(@NotNull Location location);

    @NotNull DeathChest createDeathChest(@NotNull Location location, ItemStack @NotNull ... items);

    @NotNull DeathChest createDeathChest(@NotNull Location location, @Nullable OfflinePlayer player, ItemStack @NotNull ... items);

    @NotNull DeathChest createDeathChest(@NotNull Location location, long expireAt, @Nullable OfflinePlayer player, ItemStack @NotNull ... items);

    @NotNull DeathChest createDeathChest(@NotNull DeathChestSnapshot snapshot);

    default @NotNull DeathChest createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable OfflinePlayer player, ItemStack @NotNull ... items) {
        return createDeathChest(location, createdAt, expireAt, player, false, items);
    }

    @NotNull DeathChest createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable OfflinePlayer player, boolean isProtected, ItemStack @NotNull ... items);

    @NotNull Set<@NotNull DeathChest> getChests();

    void saveChests() throws IOException;

    default boolean hasHologram() {
        return getHologramService() != null;
    }

    @Nullable IHologramService getHologramService();

    default boolean hasAnimation() {
        return getAnimationService() != null;
    }

    @Nullable IAnimationService getAnimationService();

    @NotNull IProtectionService getProtectionService();
}
