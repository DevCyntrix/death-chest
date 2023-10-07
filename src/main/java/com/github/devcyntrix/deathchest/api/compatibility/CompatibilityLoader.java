package com.github.devcyntrix.deathchest.api.compatibility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CompatibilityLoader {

    public Compatibility load(Class<? extends Compatibility> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Compatibility> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

}
