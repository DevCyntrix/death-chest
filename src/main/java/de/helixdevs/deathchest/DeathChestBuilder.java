package de.helixdevs.deathchest;

import com.google.common.base.Preconditions;
import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.config.BreakEffectOptions;
import de.helixdevs.deathchest.config.HologramOptions;
import de.helixdevs.deathchest.config.InventoryOptions;
import de.helixdevs.deathchest.config.ParticleOptions;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class DeathChestBuilder {

    private long createdAt = System.currentTimeMillis();
    private long expireAt = -1;

    private String durationFormat = "mm:ss";

    private ItemStack[] items;

    private OfflinePlayer player;

    private IHologramService hologramService;
    private IAnimationService animationService;

    private HologramOptions hologramOptions;
    private BreakEffectOptions breakEffectOptions;
    private InventoryOptions inventoryOptions;
    private ParticleOptions particleOptions;

    public long createdAt() {
        return createdAt;
    }

    public DeathChestBuilder setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public long expireAt() {
        return expireAt;
    }

    public DeathChestBuilder setExpireAt(long expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    public String durationFormat() {
        return durationFormat;
    }

    public DeathChestBuilder setDurationFormat(String durationFormat) {
        this.durationFormat = durationFormat;
        return this;
    }

    public ItemStack[] items() {
        return items;
    }

    public DeathChestBuilder setItems(ItemStack[] items) {
        this.items = items;
        return this;
    }

    public OfflinePlayer player() {
        return player;
    }

    public DeathChestBuilder setPlayer(OfflinePlayer player) {
        this.player = player;
        return this;
    }

    public IHologramService hologramService() {
        return hologramService;
    }

    public DeathChestBuilder setHologramService(IHologramService hologramService) {
        this.hologramService = hologramService;
        return this;
    }

    public IAnimationService animationService() {
        return animationService;
    }

    public DeathChestBuilder setAnimationService(IAnimationService animationService) {
        this.animationService = animationService;
        return this;
    }

    public HologramOptions hologramOptions() {
        return hologramOptions;
    }

    public DeathChestBuilder setHologramOptions(HologramOptions hologramOptions) {
        this.hologramOptions = hologramOptions;
        return this;
    }

    public InventoryOptions inventoryOptions() {
        return inventoryOptions;
    }

    public BreakEffectOptions breakEffectOptions() {
        return breakEffectOptions;
    }

    public DeathChestBuilder setBreakEffectOptions(BreakEffectOptions breakEffectOptions) {
        this.breakEffectOptions = breakEffectOptions;
        return this;
    }

    public ParticleOptions particleOptions() {
        return particleOptions;
    }

    public DeathChestBuilder setParticleOptions(ParticleOptions particleOptions) {
        this.particleOptions = particleOptions;
        return this;
    }

    public @NotNull DeathChest build(@NotNull World world, @NotNull Vector vector, @NotNull InventoryOptions inventoryOptions) {
        return build(vector.toLocation(world), inventoryOptions);
    }

    public @NotNull DeathChest build(@NotNull Location location, @NotNull InventoryOptions inventoryOptions) {
        Preconditions.checkNotNull(location.getWorld());
        this.inventoryOptions = inventoryOptions;
        return new DeathChestImpl(location, this);
    }

    public static DeathChestBuilder builder() {
        return new DeathChestBuilder();
    }

}
