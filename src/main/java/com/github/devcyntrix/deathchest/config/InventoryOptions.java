package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record InventoryOptions(
        @SerializedName("title") @NotNull String title,
        @SerializedName("size") @NotNull InventorySize size) {

    public static final String DEFAULT_TITLE = "Death Chest";

    public static final InventorySize DEFAULT_SIZE = InventorySize.FLEXIBLE;

    public Inventory createInventory(DeathChestModel model, Function<String, String> placeholder, ItemStack... stacks) {
        String title = placeholder.apply(title());
        var inventory = Bukkit.createInventory(model, size().getSize(stacks.length), title);
        inventory.setContents(stacks);
        return inventory;
    }

    @Contract("null -> new")
    public static @NotNull InventoryOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            section = new MemoryConfiguration();

        String title = ChatColor.translateAlternateColorCodes('&', section.getString("title", DEFAULT_TITLE));
        String sizeString = section.getString("size", DEFAULT_SIZE.name().toLowerCase());
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
