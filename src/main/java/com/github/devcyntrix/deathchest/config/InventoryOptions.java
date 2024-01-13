package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.DeathChestModel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record InventoryOptions(@NotNull String title, @NotNull InventorySize size) {

    public Inventory createInventory(DeathChestModel model, Function<String, String> placeholder, ItemStack... stacks) {
        String title = placeholder.apply(title());
        var inventory = Bukkit.createInventory(model, size().getSize(stacks.length), title);
        inventory.setContents(stacks);
        return inventory;
    }

    public static InventoryOptions load(ConfigurationSection section) {
        if (section == null)
            section = new MemoryConfiguration();

        String title = ChatColor.translateAlternateColorCodes('&', section.getString("title", "Death Chest"));
        String sizeString = section.getString("size", "flexible");
        InventoryOptions.InventorySize size = InventoryOptions.InventorySize.valueOf(sizeString.toUpperCase());

        return new InventoryOptions(title, size);
    }

    public enum InventorySize {
        CONSTANT(integer -> 9 * 5),
        FLEXIBLE(integer -> {
            double rel = integer / 9.0;
            int round = (int) Math.ceil(rel);
            return 9 * round;
        });

        private final Function<Integer, Integer> sizeFunction;

        InventorySize(Function<Integer, Integer> sizeFunction) {
            this.sizeFunction = sizeFunction;
        }

        public int getSize(int itemCount) {
            return sizeFunction.apply(itemCount);
        }
    }
}
