package de.helixdevs.deathchest.api.animation;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;

public interface IAnimationService {

    void spawnBlockBreakAnimation(@NotNull Vector location, @Range(from = 0, to = 9) byte state, @NotNull Collection<Player> players);

}
