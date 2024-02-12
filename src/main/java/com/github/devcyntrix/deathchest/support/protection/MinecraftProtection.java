package com.github.devcyntrix.deathchest.support.protection;

import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MinecraftProtection implements ProtectionService {

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {

        World world = Bukkit.getWorlds().get(0); // Each server has at least one world - the overworld
        if (!world.equals(location.getWorld()))
            return true;

        int spawnRadius = Bukkit.getSpawnRadius();
        if (spawnRadius <= 0)
            return true;

        Location min = world.getSpawnLocation().clone().subtract(spawnRadius, 0, spawnRadius);
        Location max = world.getSpawnLocation().clone().add(spawnRadius, 0, spawnRadius);

        return !(location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ());
    }
}
