package com.github.devcyntrix.deathchest.feature.protection;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.feature.protection.service.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProtectionService {

    private static final Map<String, Function<Plugin, com.github.devcyntrix.deathchest.api.protection.ProtectionService>> protectionServiceMap = Map.of(
            "WorldGuard", plugin -> new WorldGuardProtection(),
            "PlotSquared", plugin -> new PlotSquaredProtection(),
            "GriefPrevention", plugin -> new GriefPreventionProtection(),
            "RedProtect", plugin -> new RedProtectProtection(),
            "minePlots", plugin -> new MinePlotsProtection()
    );

    private com.github.devcyntrix.deathchest.api.protection.ProtectionService protectionService;

    public ProtectionService(DeathChestPlugin plugin) {
        List<com.github.devcyntrix.deathchest.api.protection.ProtectionService> services = new ArrayList<>();
        services.add(new MinecraftProtection());

        for (Map.Entry<String, Function<Plugin, com.github.devcyntrix.deathchest.api.protection.ProtectionService>> entry : protectionServiceMap.entrySet()) {
            if (!Bukkit.getPluginManager().isPluginEnabled(entry.getKey()))
                continue;
            com.github.devcyntrix.deathchest.api.protection.ProtectionService apply = entry.getValue().apply(plugin);
            if (apply == null)
                continue;
            plugin.debug(1, "Using " + entry.getKey() + " protection service");
            services.add(apply);
        }

        this.protectionService = new CombinedProtectionService(services.toArray(com.github.devcyntrix.deathchest.api.protection.ProtectionService[]::new));
    }

    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        return protectionService.canBuild(player, location, material);
    }
}
