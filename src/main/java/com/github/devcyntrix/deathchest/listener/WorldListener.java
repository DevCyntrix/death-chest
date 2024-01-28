package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Loads and unloads the chests if a world is loading or unloading during the runtime
 */
public class WorldListener implements Listener {

    private final DeathChestPlugin plugin;

    public WorldListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.getDeathChestController().loadChests(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        plugin.getDeathChestController().unloadChests(event.getWorld(), true);
    }

    // Method to convert the audit json items to the ingame items
//    @EventHandler
//    public void onSneak(PlayerToggleSneakEvent event) {
//        Player player = event.getPlayer();
//        if (!event.isSneaking())
//            return;
//
//        String json = "[{\"v\":3700,\"type\":\"FLINT_AND_STEEL\",\"meta\":{\"meta-type\":\"UNSPECIFIC\",\"Damage\":32}},{\"v\":3700,\"type\":\"COOKED_BEEF\",\"amount\":15},{\"v\":3700,\"type\":\"COOKED_MUTTON\"},{\"v\":3700,\"type\":\"NETHERITE_SWORD\",\"meta\":{\"meta-type\":\"UNSPECIFIC\",\"enchants\":{\"FIRE_ASPECT\":2,\"KNOCKBACK\":2,\"LOOT_BONUS_MOBS\":4,\"MENDING\":1,\"DAMAGE_ALL\":5,\"SWEEPING_EDGE\":3,\"DURABILITY\":5}}},{\"v\":3700,\"type\":\"DIAMOND_AXE\",\"meta\":{\"meta-type\":\"UNSPECIFIC\",\"Damage\":1034}},{\"v\":3700,\"type\":\"ENCHANTED_BOOK\",\"meta\":{\"meta-type\":\"ENCHANTED\",\"stored-enchants\":{\"SOUL_SPEED\":2}}},{\"v\":3700,\"type\":\"NETHERITE_PICKAXE\",\"meta\":{\"meta-type\":\"UNSPECIFIC\",\"enchants\":{\"DIG_SPEED\":5,\"LOOT_BONUS_BLOCKS\":4,\"MENDING\":1,\"DURABILITY\":4},\"Damage\":18}},{\"v\":3700,\"type\":\"ENDER_PEARL\",\"amount\":15},{\"v\":3700,\"type\":\"CHISELED_POLISHED_BLACKSTONE\",\"amount\":2},{\"v\":3700,\"type\":\"SAND\",\"amount\":44},{\"v\":3700,\"type\":\"SAND\",\"amount\":8},{\"v\":3700,\"type\":\"REDSTONE\"},{\"v\":3700,\"type\":\"DIAMOND_BOOTS\"},{\"v\":3700,\"type\":\"DIAMOND_SHOVEL\",\"meta\":{\"meta-type\":\"UNSPECIFIC\",\"enchants\":{\"DIG_SPEED\":3,\"DURABILITY\":3},\"Damage\":53}},{\"v\":3700,\"type\":\"WHITE_WOOL\",\"amount\":16}]";
//        Gson gson = new GsonBuilder()
//                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
//                .registerTypeHierarchyAdapter(ItemMeta.class, new ItemMetaAdapter())
//                .create();
//        JsonArray array = gson.fromJson(json, JsonArray.class);
//
//        for (JsonElement element : array) {
//            ItemStack item = gson.fromJson(element, ItemStack.class);
//            player.getInventory().addItem(item);
//        }
//
//    }

}
