package com.github.devcyntrix.deathchest.support.protection;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldGuardDeathChestFlag {

    public static StateFlag FLAG;

    public static void register() {
        DeathChestPlugin plugin = JavaPlugin.getPlugin(DeathChestPlugin.class);
        WorldGuard instance = WorldGuard.getInstance();
        if (instance == null) {
            plugin.debug(1, "No WorldGuard instance found.");
            return;
        }
        String name = "spawn-death-chest";
        boolean def = false;
        plugin.debug(1, "Registering \"%s\" flag (default: %s).".formatted(name, def));
        FLAG = new StateFlag(name, def);
        instance.getFlagRegistry().register(FLAG);
    }
}
