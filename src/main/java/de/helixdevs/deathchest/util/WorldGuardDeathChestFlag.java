package de.helixdevs.deathchest.util;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;

public final class WorldGuardDeathChestFlag {

    public static StateFlag FLAG;

    public static void register() {
        WorldGuard instance = WorldGuard.getInstance();
        if (instance == null)
            return;
        FLAG = new StateFlag("spawn-death-chest", false);
        instance.getFlagRegistry().register(FLAG);
    }
}
