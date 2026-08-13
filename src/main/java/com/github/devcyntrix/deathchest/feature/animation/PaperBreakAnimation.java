package com.github.devcyntrix.deathchest.feature.animation;

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
        // 修复 (fix3): 调用方可能在箱子过期后仍计算 process>1.0 → state>9 → progress>1.0
        // 导致 CraftPlayer.sendBlockDamage 抛 IllegalArgumentException: progress must be between 0.0 and 1.0
        // 防御性钳制 state 到合法范围 0-9
        state = Math.max(0, Math.min(9, state));

        @Range(from = -1, to = 9) int finalState = state;

        players.forEach(player -> {
            try {
                float progress = Math.max(0f, Math.min(1f, finalState / 9f));
                sendBlockDamageMethod.invoke(player, location.toLocation(player.getWorld()), progress, entityId);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
