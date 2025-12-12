package com.github.devcyntrix.deathchest.feature.animation;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import com.github.devcyntrix.deathchest.util.PaperTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;

public class AnimationService {

    private static final Map<String, Function<Plugin, BreakAnimationService>> animationServiceMap = Map.of(
            "ProtocolLib", plugin -> ProtocolLibrary.getProtocolManager() != null ? new ProtocolLibBreakAnimation() : null
    );

    private final BreakAnimationService breakAnimationService;

    public AnimationService(DeathChestPlugin plugin, @Nullable String preferred) {

        if (PaperTest.isPaper()) {
            plugin.debug(1, "Using paper block break animation service");
            this.breakAnimationService = new PaperBreakAnimation();
        } else {
            this.breakAnimationService = getService(animationServiceMap, plugin, preferred);
        }

    }

    public void spawnBlockBreakAnimation(@NotNull Vector location, @Range(from = -1, to = 9) int state, @NotNull Stream<? extends Player> players) {
        spawnBlockBreakAnimation(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + 10_000, // Try to avoid to use a given entity id
                location, state, players);
    }

    public void spawnBlockBreakAnimation(int entityId, @NotNull Vector location, @Range(from = -1, to = 9) int state, @NotNull Stream<? extends Player> players) {
        if (breakAnimationService != null) {
            breakAnimationService.spawnBlockBreakAnimation(entityId, location, state, players);
        }
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
