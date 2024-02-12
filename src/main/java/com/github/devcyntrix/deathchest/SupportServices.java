package com.github.devcyntrix.deathchest;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import com.github.devcyntrix.deathchest.support.animation.PaperBreakAnimation;
import com.github.devcyntrix.deathchest.support.animation.ProtocolLibBreakAnimation;
import com.github.devcyntrix.deathchest.support.protection.*;
import com.github.devcyntrix.deathchest.util.PaperTest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class SupportServices {

    private static final Map<String, Function<Plugin, BreakAnimationService>> animationServiceMap = Map.of(
            "ProtocolLib", plugin -> ProtocolLibrary.getProtocolManager() != null ? new ProtocolLibBreakAnimation() : null
    );

    private static final Map<String, Function<Plugin, ProtectionService>> protectionServiceMap = Map.of(
            "WorldGuard", plugin -> new WorldGuardProtection(),
            "PlotSquared", plugin -> new PlotSquaredProtection(),
            "GriefPrevention", plugin -> new GriefPreventionProtection(),
            "RedProtect", plugin -> new RedProtectProtection(),
            "minePlots", plugin -> new MinePlotsProtection()
    );

    public static @Nullable BreakAnimationService getBlockBreakAnimationService(@NotNull DeathChestPlugin plugin, @Nullable String preferred) {
        if (PaperTest.isPaper()) {
            plugin.debug(1, "Using paper block break animation service");
            return new PaperBreakAnimation();
        }
        return getService(animationServiceMap, plugin, preferred);
    }

    public static @NotNull ProtectionService getProtectionService(@NotNull DeathChestPlugin plugin) {
        List<ProtectionService> services = new ArrayList<>();
        services.add(new MinecraftProtection());
        for (Map.Entry<String, Function<Plugin, ProtectionService>> entry : protectionServiceMap.entrySet()) {
            if (!Bukkit.getPluginManager().isPluginEnabled(entry.getKey()))
                continue;
            ProtectionService apply = entry.getValue().apply(plugin);
            if (apply == null)
                continue;
            plugin.debug(1, "Using " + entry.getKey() + " protection service");
            services.add(apply);
        }
        return new CombinedProtectionService(services.toArray(ProtectionService[]::new));
    }

    private static <T> @Nullable T getService(@NotNull Map<String, Function<Plugin, T>> map, @NotNull Plugin plugin, @Nullable String preferred) {
        T service;

        if (preferred != null) {
            if (Bukkit.getPluginManager().isPluginEnabled(preferred)) {
                Function<Plugin, T> func = map.get(preferred);
                if (func != null) {
                    service = func.apply(plugin);
                    if (service != null)
                        return service;
                }
            }
            plugin.getLogger().warning("Cannot use the preferred service \"%s\"".formatted(preferred));
        }

        for (Map.Entry<String, Function<Plugin, T>> entry : map.entrySet()) {
            if (!Bukkit.getPluginManager().isPluginEnabled(entry.getKey()))
                continue;

            Function<Plugin, T> value = entry.getValue();
            T apply = value.apply(plugin);
            if (apply == null) {
                plugin.getLogger().warning("Failed to initialize the service \"%s\"".formatted(entry.getKey()));
                continue;
            }
            return apply;
        }
        return null;
    }
}
