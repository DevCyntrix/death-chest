package de.helixdevs.deathchest.util;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public final class EntityId {

    private static Class<?> entityClass = null;

    static {
        try {
            entityClass = Class.forName("net.minecraft.world.entity.Entity");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static int increaseAndGet() {
        if (entityClass == null)
            return -1;
        try {
            Field c = entityClass.getDeclaredField("c");
            if (!c.trySetAccessible())
                return -1;
            AtomicInteger integer = (AtomicInteger) c.get(null);
            return integer.incrementAndGet();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return -1;
    }

}
