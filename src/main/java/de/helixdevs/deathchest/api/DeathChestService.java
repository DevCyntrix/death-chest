package de.helixdevs.deathchest.api;

import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface DeathChestService {

    @NotNull DeathChest createDeathChest(@NotNull Location location, ItemStack @NotNull ... stacks);

    @NotNull DeathChest createDeathChest(@NotNull Location location, @Nullable OfflinePlayer player, ItemStack @NotNull ... stacks);

    @NotNull DeathChest createDeathChest(@NotNull Location location, long expireAt, @Nullable OfflinePlayer player, ItemStack @NotNull ... stacks);

    @NotNull DeathChest createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable OfflinePlayer player, ItemStack @NotNull ... stacks);

    @NotNull Set<@NotNull DeathChest> getChests();

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
