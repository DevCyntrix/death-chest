package com.github.devcyntrix.deathchest.feature.lock;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.compatibility.Compatibility;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestService;
import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import org.bukkit.Server;

public class LWCCompatibility extends Compatibility {

    @Override
    public boolean isValid(Server server) {
        return server.getPluginManager().isPluginEnabled("LWC");
    }

    @Override
    protected void enable(DeathChestPlugin plugin) {
        try {
            Class.forName("com.griefcraft.lwc.LWC");
            LWC.getInstance().getModuleLoader().registerModule(plugin, new LWCModule(plugin));
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    protected void disable(DeathChestPlugin plugin) {
        try {
            Class.forName("com.griefcraft.lwc.LWC");
            LWC.getInstance().getModuleLoader().removeModules(plugin);
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static class LWCModule extends JavaModule {

        private final DeathChestPlugin plugin;

        public LWCModule(DeathChestPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void onRegisterProtection(LWCProtectionRegisterEvent event) {
            DeathChestService controller = plugin.getDeathChestService();
            if (controller.getChest(event.getBlock().getLocation()) != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getPrefix() + "§cYou cannot lock this chest.");
            }
        }
    }

}
