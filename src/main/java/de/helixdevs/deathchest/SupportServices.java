package de.helixdevs.deathchest;

import com.comphenix.protocol.ProtocolLibrary;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import de.helixdevs.deathchest.support.animation.ProtocolLibAnimation;
import de.helixdevs.deathchest.support.protection.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public final class SupportServices {

    private static final Map<String, Function<Plugin, IAnimationService>> animationServiceMap = Map.of(
            "ProtocolLib", plugin -> ProtocolLibrary.getProtocolManager() != null ? new ProtocolLibAnimation() : null
    );

    private static final Map<String, Function<Plugin, IProtectionService>> protectionServiceMap = Map.of(
            "WorldGuard", plugin -> new WorldGuardProtection(),
            "PlotSquared", plugin -> new PlotSquaredProtection(),
            "GriefPrevention", plugin -> new GriefPreventionProtection(),
            "RedProtect", plugin -> new RedProtection(),
            "GriefDefender", plugin -> new GriefDefenderProtectionService()
    );

    public static @Nullable IAnimationService getAnimationService(@NotNull Plugin plugin, @Nullable String preferred) {
        return getService(animationServiceMap, plugin, preferred);
    }

    public static @Nullable IProtectionService getProtectionService(@NotNull Plugin plugin) {
        IProtectionService[] services = protectionServiceMap.entrySet().stream()
                .filter(entry -> Bukkit.getPluginManager().isPluginEnabled(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(f -> f.apply(plugin))
                .toArray(IProtectionService[]::new);
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
            plugin.getLogger().warning("Cannot use the preferred hologram service \"%s\"".formatted(preferred));
        }

        for (Map.Entry<String, Function<Plugin, T>> entry : map.entrySet()) {
            if (!Bukkit.getPluginManager().isPluginEnabled(entry.getKey()))
                continue;

            Function<Plugin, T> value = entry.getValue();
            T apply = value.apply(plugin);
            if (apply == null) {
                plugin.getLogger().warning("Failed to initialize the hologram service \"%s\"".formatted(entry.getKey()));
                continue;
            }
            return apply;
        }
        return null;
    }
}
