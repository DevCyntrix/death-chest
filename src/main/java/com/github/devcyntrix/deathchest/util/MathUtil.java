package com.github.devcyntrix.deathchest.util;

public final class MathUtil {

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

}
