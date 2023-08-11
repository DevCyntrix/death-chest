package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.deathchest.api.hologram.Hologram;
import com.github.devcyntrix.deathchest.config.InventoryOptions;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
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
import org.jetbrains.annotations.NotNull;
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

    public DeathChestModel(Location location, long createdAt, long expireAt, OfflinePlayer owner, boolean isProtected) {
        this.location = location;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.owner = owner;
        this.isProtected = isProtected;
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
        for (Closeable closeable : tasks) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tasks.clear();
    }

    public void dropItems() {
        dropItems(getLocation());
    }

    public void dropItems(@NotNull Location location) {
        Preconditions.checkNotNull(location.getWorld(), "invalid location");
        for (ItemStack itemStack : getInventory()) {
            if (itemStack == null) continue;
            getWorld().dropItemNaturally(location, itemStack); // World won't be null
        }
        inventory.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeathChestModel that = (DeathChestModel) o;
        return createdAt == that.createdAt && Objects.equal(location, that.location) && Objects.equal(owner.getUniqueId(), that.owner.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(location, createdAt, owner.getUniqueId());
    }

    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("location", getLocation());
        map.put("createdAt", getCreatedAt());
        map.put("expireAt", getExpireAt());
        if (getOwner() != null)
            map.put("player", getOwner().getUniqueId().toString());
        map.put("protected", isProtected());
        map.put("items", getInventory().getContents());
        return map;
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
        var model = new DeathChestModel(location, createdAt, expireAt, owner, isProtected);
        model.setInventory(options.createInventory(model, s -> s, itemStacks));
        return model;
    }

}
