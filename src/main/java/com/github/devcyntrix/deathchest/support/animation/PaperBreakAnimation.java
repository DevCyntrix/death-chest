package com.github.devcyntrix.deathchest.support.animation;

import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class PaperBreakAnimation implements BreakAnimationService {

    static Method sendBlockDamageMethod;

    static {
        try {
            sendBlockDamageMethod = Player.class.getMethod("sendBlockDamage", Location.class, float.class, int.class);
        } catch (NoSuchMethodException ignored) {
        }
    }

    @Override
    public void spawnBlockBreakAnimation(int entityId, @NotNull Vector location, @Range(from = -1, to = 9) int state, @NotNull Stream<? extends Player> players) {
        if (sendBlockDamageMethod == null)
            return;
        if (state == -1) {
            state = 0;
        }

        @Range(from = -1, to = 9) int finalState = state;

        players.forEach(player -> {
            try {
                sendBlockDamageMethod.invoke(player, location.toLocation(player.getWorld()), finalState / 9f, entityId);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
