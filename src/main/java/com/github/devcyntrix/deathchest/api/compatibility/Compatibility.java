package com.github.devcyntrix.deathchest.api.compatibility;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import lombok.Getter;
import org.bukkit.Server;

@Getter
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
            e.printStackTrace();
            try {
                disable(plugin);
            } catch (Exception e1) {
                e1.printStackTrace();
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
}
