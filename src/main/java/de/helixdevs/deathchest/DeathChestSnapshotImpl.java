package de.helixdevs.deathchest;

import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.DeathChestSnapshot;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DeathChestSnapshotImpl implements DeathChestSnapshot {

    private final Location location;
    private final long createdAt;
    private final long expireAt;
    private final OfflinePlayer owner;
    private final ItemStack[] items;

    DeathChestSnapshotImpl(DeathChest chest) {
        this.location = chest.getLocation().clone();
        this.createdAt = chest.getCreatedAt();
        this.expireAt = chest.getExpireAt();
        this.owner = chest.getPlayer();
        this.items = chest.getInventory().getContents();
    }

    private DeathChestSnapshotImpl(Location location, long createdAt, long expireAt, OfflinePlayer owner, ItemStack... items) {
        this.location = location;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.owner = owner;
        this.items = items;
    }

    @NotNull
    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public long getExpireAt() {
        return expireAt;
    }

    @Nullable
    @Override
    public OfflinePlayer getOwner() {
        return owner;
    }

    @NotNull
    @Override
    public ItemStack @NotNull [] getItems() {
        return items;
    }
}
