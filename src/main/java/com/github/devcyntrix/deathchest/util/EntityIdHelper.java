package com.github.devcyntrix.deathchest.util;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class EntityIdHelper {

    private static Method getHandleMethod;

    private static Method nextEntityIdMethod;

    private static AtomicInteger counter;

    static {
        if (DeathChestPlugin.isTest()) {
            counter = new AtomicInteger(0);
        } else {
            try {
                Class<?> entityClass = Class.forName("net.minecraft.world.entity.Entity");
                Field entityCounter = Arrays.stream(entityClass.getDeclaredFields())
                        .filter(field -> field.getType().equals(AtomicInteger.class))
                        .findFirst()
                        .orElse(null);
                if (entityCounter != null && entityCounter.trySetAccessible()) {
                    counter = (AtomicInteger) entityCounter.get(null);
                }

                if (entityCounter == null) {
                    // CraftBukkit
                    Class<?> craftWorldClass = Class.forName("org.bukkit.craftbukkit.CraftWorld");
                    getHandleMethod = craftWorldClass.getMethod("getHandle");

                    // NMS Server
                    Class<?> serverLevelClass = Class.forName("net.minecraft.server.level.ServerLevel");
                    nextEntityIdMethod = serverLevelClass.getMethod("getNextEntityId");
                }
            } catch (Exception e) {
                e.printStackTrace();
                counter = new AtomicInteger();
            }


        }
    }

    public static int increaseAndGet(World world) {
        if (counter != null) {
            return counter.incrementAndGet();
        }

        try {
            var serverLevel = getHandleMethod.invoke(world);
            return (int) nextEntityIdMethod.invoke(serverLevel);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
