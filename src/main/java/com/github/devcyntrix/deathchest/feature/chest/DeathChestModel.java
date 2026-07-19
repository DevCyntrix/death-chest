package com.github.devcyntrix.deathchest.feature.chest;

import com.github.devcyntrix.deathchest.config.InventoryOptions;
import com.github.devcyntrix.deathchest.feature.placeholder.PlaceholderService;
import com.github.devcyntrix.hologram.api.Hologram;
import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
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
import org.jspecify.annotations.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

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

    public DeathChestModel() {
    }

    public DeathChestModel(Location location, long createdAt, long expireAt, @Nullable OfflinePlayer owner, boolean isProtected) {
        this.location = location;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.owner = owner;
        this.isProtected = isProtected;
    }

    public DeathChestModel(Location location, long createdAt, long expireAt, @Nullable OfflinePlayer owner, boolean isProtected, Inventory inventory, BlockState previous, Hologram hologram, Integer breakingEntityId, boolean isDeleting, Set<Closeable> tasks) {
        this.location = location;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.owner = owner;
        this.isProtected = isProtected;
        this.inventory = inventory;
        this.previous = previous;
        this.hologram = hologram;
        this.breakingEntityId = breakingEntityId;
        this.isDeleting = isDeleting;
        this.tasks = tasks;
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

    public static DeathChestModel deserialize(Map<String, Object> map, InventoryOptions options, PlaceholderService controller) {
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public @Nullable OfflinePlayer getOwner() {
        return owner;
    }

    public void setOwner(@Nullable OfflinePlayer owner) {
        this.owner = owner;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public BlockState getPrevious() {
        return previous;
    }

    public void setPrevious(BlockState previous) {
        this.previous = previous;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public Integer getBreakingEntityId() {
        return breakingEntityId;
    }

    public void setBreakingEntityId(Integer breakingEntityId) {
        this.breakingEntityId = breakingEntityId;
    }

    public boolean isDeleting() {
        return isDeleting;
    }

    public void setDeleting(boolean deleting) {
        isDeleting = deleting;
    }

    public Set<Closeable> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Closeable> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "DeathChestModel{" +
                "location=" + location +
                ", createdAt=" + createdAt +
                ", expireAt=" + expireAt +
                ", owner=" + owner +
                ", isProtected=" + isProtected +
                ", inventory=" + inventory +
                ", previous=" + previous +
                ", hologram=" + hologram +
                ", breakingEntityId=" + breakingEntityId +
                ", isDeleting=" + isDeleting +
                ", tasks=" + tasks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeathChestModel that = (DeathChestModel) o;
        return createdAt == that.createdAt && expireAt == that.expireAt && isProtected == that.isProtected && isDeleting == that.isDeleting && Objects.equals(location, that.location) && Objects.equals(owner, that.owner) && Objects.equals(inventory, that.inventory) && Objects.equals(previous, that.previous) && Objects.equals(hologram, that.hologram) && Objects.equals(breakingEntityId, that.breakingEntityId) && Objects.equals(tasks, that.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, createdAt, expireAt, owner, isProtected, inventory, previous, hologram, breakingEntityId, isDeleting, tasks);
    }
}
