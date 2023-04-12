package com.github.devcyntrix.deathchest.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class EntityIdHelper {
    private static AtomicInteger counter;

    private static void initCounter() {
        try {
            Class<?> entityClass = Class.forName("net.minecraft.world.entity.Entity");
            Field entityCounter = Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.getType().equals(AtomicInteger.class))
                    .findFirst()
                    .orElse(null);
            if (entityCounter == null)
                return;
            entityCounter.trySetAccessible();
            counter = (AtomicInteger) entityCounter.get(null);
        } catch (ClassNotFoundException | IllegalAccessException ignored) {
        } finally {
            if (counter == null) counter = new AtomicInteger();
        }
    }

    public static int increaseAndGet() {
        if (counter == null)
            initCounter();
        return counter.incrementAndGet();
    }
}
