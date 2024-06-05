package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.util.LocationUtil;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class DropConditionConfig {

    @SerializedName("void-death")
    private boolean voidDeath;
    @SerializedName("lava-death")
    private boolean lavaDeath;
    @SerializedName("fire-death")
    private boolean fireDeath;

    @Getter
    private final transient Set<DropCondition> appliedConditions = new HashSet<>();

    public DropConditionConfig(boolean voidDeath, boolean lavaDeath, boolean fireDeath) {
        this.voidDeath = voidDeath;
        this.lavaDeath = lavaDeath;
        this.fireDeath = fireDeath;

        if (voidDeath) appliedConditions.add(location -> {
            return location.getBlockY() < location.getWorld().getMinHeight();
        });
        if (lavaDeath) appliedConditions.add(location -> {
            return LocationUtil.isValidBlock(location) && location.getBlock().getType() == Material.LAVA;
        });
        if (fireDeath) appliedConditions.add(location -> {
            return LocationUtil.isValidBlock(location) && location.getBlock().getType() == Material.FIRE;
        });
    }

    public boolean shouldDrop(Location location) {
        return appliedConditions.stream().anyMatch(dropCondition -> dropCondition.shouldDropItems(location));
    }

    @Contract("null -> new")
    public static @NotNull DropConditionConfig load(@Nullable ConfigurationSection section) {
        if (section == null) section = new MemoryConfiguration();

        boolean voidDeath = section.getBoolean("void-death", false);
        boolean lavaDeath = section.getBoolean("lava-death", false);
        boolean fireDeath = section.getBoolean("fire-death", false);

        return new DropConditionConfig(voidDeath, lavaDeath, fireDeath);
    }

}
