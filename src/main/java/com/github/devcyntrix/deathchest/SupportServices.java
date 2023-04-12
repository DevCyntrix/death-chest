package com.github.devcyntrix.deathchest;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.devcyntrix.deathchest.api.animation.AnimationService;
import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import com.github.devcyntrix.deathchest.support.animation.PaperAnimation;
import com.github.devcyntrix.deathchest.support.animation.ProtocolLibAnimation;
import com.github.devcyntrix.deathchest.support.protection.*;
import com.github.devcyntrix.deathchest.util.PaperTest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public final class SupportServices {

    private static final Map<String, Function<Plugin, AnimationService>> animationServiceMap = Map.of(
            "ProtocolLib", plugin -> ProtocolLibrary.getProtocolManager() != null ? new ProtocolLibAnimation() : null
    );

    private static final Map<String, Function<Plugin, ProtectionService>> protectionServiceMap = Map.of(
            "WorldGuard", plugin -> new WorldGuardProtection(),
            "PlotSquared", plugin -> new PlotSquaredProtection(),
            "GriefPrevention", plugin -> new GriefPreventionProtection(),
            "RedProtect", plugin -> new RedProtection(),
            "GriefDefender", plugin -> new GriefDefenderProtectionService()
    );

    public static @Nullable AnimationService getAnimationService(@NotNull Plugin plugin, @Nullable String preferred) {
        if (PaperTest.isPaper()) {
            return new PaperAnimation();
        }
        return getService(animationServiceMap, plugin, preferred);
    }

    public static @Nullable ProtectionService getProtectionService(@NotNull Plugin plugin) {
        ProtectionService[] services = protectionServiceMap.entrySet().stream()
                .filter(entry -> Bukkit.getPluginManager().isPluginEnabled(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(f -> f.apply(plugin))
                .toArray(ProtectionService[]::new);
        return new CombinedProtectionService(services);
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
