package de.helixdevs.deathchest;

import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.DeathChestSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private DeathChestSnapshotImpl(Location location, long createdAt, long expireAt, OfflinePlayer owner, ItemStack[] items) {
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

    public static DeathChestSnapshot deserialize(Map<String, Object> map) {
        long createdAt = Long.parseLong(map.get("createdAt").toString());
        long expireAt = Long.parseLong(map.get("expireAt").toString());

        if (expireAt != -1 && expireAt <= System.currentTimeMillis()) // Expire here
            return null;

        Location location = (Location) map.get("location");
        if (location == null)
            return null;

        String player = (String) map.get("player");
        UUID playerId = player == null ? null : UUID.fromString(player);
        OfflinePlayer owner = playerId != null ? Bukkit.getOfflinePlayer(playerId) : null;

        List<ItemStack> stacks = (List<ItemStack>) map.get("items");
        if (stacks == null)
            return null;
        ItemStack[] itemStacks = stacks.toArray(ItemStack[]::new);

        return new DeathChestSnapshotImpl(location, createdAt, expireAt, owner, itemStacks);
    }
}
