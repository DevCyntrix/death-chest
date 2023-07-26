package com.github.devcyntrix.deathchest.api;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.api.animation.AnimationService;
import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import com.github.devcyntrix.hologram.api.HologramService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.stream.Stream;

public interface DeathChestService {

    @Nullable DeathChestModel getLastChest(@NotNull Player player);

    boolean canPlaceChestAt(@NotNull Location location);

    @NotNull DeathChestModel createDeathChest(@NotNull Location location, ItemStack @NotNull ... items);

    @NotNull DeathChestModel createDeathChest(@NotNull Location location, @Nullable Player player, ItemStack @NotNull ... items);

    @NotNull DeathChestModel createDeathChest(@NotNull Location location, long expireAt, @Nullable Player player, ItemStack @NotNull ... items);

    default @NotNull DeathChestModel createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable Player player, ItemStack @NotNull ... items) {
        return createDeathChest(location, createdAt, expireAt, player, false, items);
    }

    @NotNull DeathChestModel createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable Player player, boolean isProtected, ItemStack @NotNull ... items);

    @NotNull Stream<@NotNull DeathChestModel> getChests();

    @NotNull Stream<@NotNull DeathChestModel> getChests(@NotNull World world);

    void saveChests() throws IOException;

    default boolean hasHologram() {
        return getHologramService() != null;
    }

    @Nullable HologramService getHologramService();

    default boolean hasAnimation() {
        return getAnimationService() != null;
    }

    @Nullable AnimationService getAnimationService();

    @NotNull ProtectionService getProtectionService();
}
