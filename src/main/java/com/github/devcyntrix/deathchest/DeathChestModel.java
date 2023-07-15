package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.deathchest.api.hologram.Hologram;
import com.github.devcyntrix.deathchest.config.InventoryOptions;
import com.github.devcyntrix.deathchest.util.EntityIdHelper;
import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class DeathChestModel {

    private Location location;
    private long createdAt;
    private long expireAt;
    private OfflinePlayer owner;
    private boolean isProtected;
    private Inventory inventory;

    private transient BlockState previous;
    private transient Hologram hologram;
    private transient Integer breakingEntityId;

    private transient Set<Closeable> tasks = new HashSet<>();

    public DeathChestModel(Location location, long createdAt, long expireAt, OfflinePlayer owner, boolean isProtected, Inventory inventory) {
        this.location = location;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.owner = owner;
        this.isProtected = isProtected;
        this.inventory = inventory;
    }

    @Nullable
    public World getWorld() {
        if (getLocation() == null)
            return null;
        return getLocation().getWorld();
    }

    public boolean isExpiring() {
        return this.expireAt > 0;
    }

    public void cancelTasks() {
        for (Closeable closeable : this.tasks) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeathChestModel that = (DeathChestModel) o;
        return createdAt == that.createdAt && Objects.equal(location, that.location) && Objects.equal(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(location, createdAt, owner);
    }

    public static DeathChestModel deserialize(Map<String, Object> map, InventoryOptions options) {
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

        boolean isProtected = false;
        Object o = map.get("protected");
        if (o != null) {
            isProtected = Boolean.parseBoolean(o.toString());
        }

        List<ItemStack> stacks = (List<ItemStack>) map.get("items");
        if (stacks == null)
            return null;
        var itemStacks = stacks.toArray(ItemStack[]::new);
        var inventory = options.createInventory(s -> s, itemStacks);

        return new DeathChestModel(location, createdAt, expireAt, owner, isProtected, inventory);
    }

}
