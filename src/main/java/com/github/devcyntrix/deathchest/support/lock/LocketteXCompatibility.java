package com.github.devcyntrix.deathchest.support.lock;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.compatibility.Compatibility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;

public final class LocketteXCompatibility extends Compatibility {

    private static final String eventClassPath = "pro.dracarys.LocketteX.api.PlayerProtectBlockEvent";

    private Listener instance;

    @Override
    public boolean isValid(Server server) {
        return server.getPluginManager().isPluginEnabled("LocketteX");
    }

    @Override
    protected void enable(DeathChestPlugin plugin) {
        try {
            Class<?> aClass1 = Class.forName(eventClassPath);
            if (!Event.class.isAssignableFrom(aClass1)) {
                plugin.getLogger().severe("Event is not assignable from " + eventClassPath);
                plugin.getLogger().severe("Cannot initialize the LocketteX compatibility");
                return;
            }
            Class<? extends Event> subclass = aClass1.asSubclass(Event.class);
            instance = new Listener() {
            };
            Bukkit.getPluginManager().registerEvent(subclass, instance, EventPriority.NORMAL, (listener, event) -> {
                try {
                    Location location = (Location) subclass.getMethod("getLocation").invoke(event);
                    if (plugin.getDeathChestController().getChest(location) != null) {
                        subclass.getMethod("setCancelled", boolean.class).invoke(event, true);

                        Player player = (Player) subclass.getMethod("getPlayer").invoke(event);
                        player.sendMessage(plugin.getPrefix() + "§cYou cannot lock this chest.");
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                    HandlerList.unregisterAll(instance);
                    plugin.getLogger().severe("Failed to handle the LocketteX event.");
                }
            }, plugin, true);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to initialize the LocketteX compatibility");
        }
    }

    @Override
    protected void disable(DeathChestPlugin plugin) {
        HandlerList.unregisterAll(instance);
    }
}
