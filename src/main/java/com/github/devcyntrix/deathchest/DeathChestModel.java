package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.deathchest.config.InventoryOptions;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.github.devcyntrix.hologram.api.Hologram;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class DeathChestModel implements InventoryHolder {

    @Expose
    private Location location;
    @Expose
    private long createdAt;
    @Expose
    private long expireAt;
    @Expose
    @Nullable
    private OfflinePlayer owner;
    @Expose
    private boolean isProtected;
    @Expose(deserialize = false)
    private Inventory inventory;

    private transient BlockState previous;
    private transient Hologram hologram;
    private transient Integer breakingEntityId;
    private transient boolean isDeleting;

    private transient Set<Closeable> tasks = new HashSet<>();

    public DeathChestModel(Location location, long createdAt, long expireAt, @Nullable OfflinePlayer owner, boolean isProtected) {
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

    public boolean isExpired() {
        return this.expireAt < System.currentTimeMillis();
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
        Preconditions.checkNotNull(location.getWorld(), "invalid location because world is null");
        for (ItemStack itemStack : getInventory()) {
            if (itemStack == null) continue;
            location.getWorld().dropItemNaturally(location, itemStack); // World won't be null
        }
        inventory.clear();
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

    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("location", getLocation());
        map.put("createdAt", getCreatedAt());
        map.put("expireAt", getExpireAt());
        if (getOwner() != null)
            map.put("player", getOwner().getUniqueId().toString());
        map.put("protected", isProtected());
        ItemStack[] array = Arrays.stream(getInventory().getContents()).filter(itemStack -> itemStack != null && !itemStack.getType().isAir()).toArray(ItemStack[]::new);
        map.put("items", array);
        return map;
    }

    public static DeathChestModel deserialize(Map<String, Object> map, InventoryOptions options, PlaceholderController controller) {
        long createdAt = Long.parseLong(map.get("createdAt").toString());
        long expireAt = Long.parseLong(map.get("expireAt").toString());

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

        model.setInventory(options.createInventory(model, s -> controller.replace(model, s), itemStacks));
        return model;
    }

}
