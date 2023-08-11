package com.github.devcyntrix.deathchest.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The listener calculates the number of experience bottles to be able to store the levels in the form of bottles in the chest.
 * For this it uses the average of experience points, which drops an experience bottle
 */
public class ConvertExpToBottleListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        int droppedExp = event.getDroppedExp();
        int numberOfBottles = droppedExp / 7; // Seven is the average of experience points which drops a Bottle o' Enchanting (https://minecraft.fandom.com/wiki/Bottle_o%27_Enchanting#Usage)

        do {
            int size = Math.min(numberOfBottles, 64);
            event.getDrops().add(new ItemStack(Material.EXPERIENCE_BOTTLE, size));
            numberOfBottles -= size;
        } while (numberOfBottles > 0);
        event.setDroppedExp(0);
    }


}
