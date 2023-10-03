package com.github.devcyntrix.deathchest.support.lock;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;

public final class LWCCompatibility {

    public static void init(DeathChestPlugin plugin) {
        LWC.getInstance().getModuleLoader().registerModule(plugin, new JavaModule() {
            @Override
            public void onRegisterProtection(LWCProtectionRegisterEvent event) {
                if (plugin.getDeathChestController().getChest(event.getBlock().getLocation()) != null) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(plugin.getPrefix() + "§cYou cannot lock this chest.");
                }
            }
        });
    }

    public static void terminate(DeathChestPlugin plugin) {
        LWC.getInstance().getModuleLoader().removeModules(plugin);
    }

}
