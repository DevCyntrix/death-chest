package de.helixdevs.deathchest.util;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class WorldGuardDeathChestFlag {

    public static StateFlag FLAG;

    public static void register() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = pluginManager.getPlugin("WorldGuard");
        if (plugin == null)
            return;
        WorldGuard instance = WorldGuard.getInstance();
        if (instance == null)
            return;
        FLAG = new StateFlag("spawn-death-chest", false);
        instance.getFlagRegistry().register(FLAG);
    }

}
