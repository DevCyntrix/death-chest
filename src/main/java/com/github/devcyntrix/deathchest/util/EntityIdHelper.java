package com.github.devcyntrix.deathchest.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class EntityIdHelper {
    private static AtomicInteger counter;

    static {
        try {
            Class<?> entityClass = Class.forName("net.minecraft.world.entity.Entity");
            Field entityCounter = Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.getType().equals(AtomicInteger.class))
                    .findFirst()
                    .orElse(null);
            if (entityCounter != null) {
                entityCounter.trySetAccessible();
                counter = (AtomicInteger) entityCounter.get(null);
            }
        } catch (Exception e) {
            counter = new AtomicInteger();
            e.printStackTrace();
        }
    }

    public static int increaseAndGet() {
        return counter.incrementAndGet();
    }
}
