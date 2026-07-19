package com.github.devcyntrix.deathchest.api.compatibility;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import org.bukkit.Server;

import java.util.logging.Level;

public abstract class Compatibility {

    private boolean enabled;

    public abstract boolean isValid(Server server);

    protected abstract void enable(DeathChestPlugin plugin);

    protected abstract void disable(DeathChestPlugin plugin);

    public boolean activate(DeathChestPlugin plugin) {
        if (enabled)
            return false;

        try {
            enable(plugin);
            this.enabled = true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to enable compatibility %s".formatted(getClass().getCanonicalName()), e);
            try {
                disable(plugin);
            } catch (Exception e1) {
                plugin.getLogger().log(Level.WARNING, "Failed to disable compatibility %s".formatted(getClass().getCanonicalName()), e1);
            }
        }
        return enabled;
    }

    public boolean deactivate(DeathChestPlugin plugin) {
        if (!enabled)
            return false;
        try {
            this.enabled = false;
            disable(plugin);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
