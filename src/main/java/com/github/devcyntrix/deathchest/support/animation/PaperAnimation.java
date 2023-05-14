package com.github.devcyntrix.deathchest.support.animation;

import com.github.devcyntrix.deathchest.api.animation.AnimationService;
import com.github.devcyntrix.deathchest.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class PaperAnimation implements AnimationService {

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

        float relativeState = MathUtil.clamp(state / 9f, 0f, 1f);

        players.forEach(player -> {
            try {
                sendBlockDamageMethod.invoke(player, location.toLocation(player.getWorld()), relativeState, entityId);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
