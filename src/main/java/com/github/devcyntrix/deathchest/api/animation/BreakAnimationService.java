package com.github.devcyntrix.deathchest.api.animation;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public interface BreakAnimationService {

    default void spawnBlockBreakAnimation(@NotNull Vector location, @Range(from = -1, to = 9) int state, @NotNull Stream<? extends Player> players) {
        spawnBlockBreakAnimation(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + 10_000, // Try to avoid to use a given entity id
                location, state, players);
    }

    void spawnBlockBreakAnimation(int entityId, @NotNull Vector location, @Range(from = -1, to = 9) int state, @NotNull Stream<? extends Player> players);
}
