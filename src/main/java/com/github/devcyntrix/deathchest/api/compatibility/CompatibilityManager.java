package com.github.devcyntrix.deathchest.api.compatibility;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class CompatibilityManager {

    private final Set<Class<? extends Compatibility>> registeredCompatibilities = new HashSet<>();
    private final Map<Class<? extends Compatibility>, Compatibility> instances = new HashMap<>();

    private final DeathChestPlugin plugin;
    private final CompatibilityLoader loader;

    public boolean registerCompatibility(Class<? extends Compatibility> clazz) {
        return this.registeredCompatibilities.add(clazz);
    }

    public boolean unregisterCompatibility(Class<? extends Compatibility> clazz) {
        if (registeredCompatibilities.remove(clazz)) {
            Compatibility compatibility = instances.remove(clazz);
            if (compatibility == null)
                return true;
            if (!compatibility.isEnabled())
                return true;
            return compatibility.deactivate(plugin);
        }
        return false;
    }

    public void enableCompatibilities() {
        if (!instances.isEmpty())
            throw new IllegalStateException("Already compatibilities enabled");

        for (Class<? extends Compatibility> rc : registeredCompatibilities) {
            try {
                Compatibility load = loader.load(rc);
                if (!load.isValid(plugin.getServer()))
                    continue;
                if (!load.activate(plugin)) {
                    plugin.getLogger().severe("Something went wrong while activating " + rc.getSimpleName());
                    continue;
                }
                this.instances.put(rc, load);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                plugin.getLogger().severe("Failed to load " + rc.getSimpleName());
            } catch (NoClassDefFoundError ignored) {
            }
        }
    }

    public void disableCompatibilities() {
        for (Compatibility compatibility : this.instances.values()) {
            if (!compatibility.isEnabled())
                continue;
            if (!compatibility.deactivate(plugin)) {
                plugin.getLogger().severe("Something went wrong while deactivating " + compatibility.getClass().getSimpleName());
            }
        }
    }
}
