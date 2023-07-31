package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.stream.Stream;

public class BreakAnimationController implements BreakAnimationService {

    @Override
    public void spawnBlockBreakAnimation(int entityId, @NotNull Vector location, @Range(from = -1, to = 9) int state, @NotNull Stream<? extends Player> players) {

    }
}
