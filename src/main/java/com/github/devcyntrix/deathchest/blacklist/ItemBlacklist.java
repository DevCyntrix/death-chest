package com.github.devcyntrix.deathchest.blacklist;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemBlacklist implements InventoryHolder {

    public static final ItemStack ADD_ITEM = new ItemStack(Material.EMERALD_BLOCK);

    public static final ItemStack FORCE_ADD_ITEM = new ItemStack(Material.LAPIS_BLOCK);

    public static final ItemStack DENY_ITEM = new ItemStack(Material.RED_STAINED_GLASS_PANE);

    static {
        ItemMeta itemMeta = ADD_ITEM.getItemMeta();
        itemMeta.setDisplayName("§rAdd item");
        ADD_ITEM.setItemMeta(itemMeta);


        itemMeta = FORCE_ADD_ITEM.getItemMeta();
        itemMeta.setDisplayName("§rForce add item");
        itemMeta.setLore(Arrays.asList("§7This will remove all items from the blacklist which falls into the scheme."));
        FORCE_ADD_ITEM.setItemMeta(itemMeta);


        itemMeta = DENY_ITEM.getItemMeta();
        itemMeta.setDisplayName("§cItem not found");
        DENY_ITEM.setItemMeta(itemMeta);

    }


    private final File file;
    private final Set<ItemStack> list = new HashSet<>();
    private final Inventory inventory;

    public ItemBlacklist(File file) {
        this.file = file;
        if (file.isFile()) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            this.list.addAll((Collection<? extends ItemStack>) yamlConfiguration.getList("blacklist"));
        }

        this.inventory = Bukkit.createInventory(this, 9 * 6, "Blacklist");
        inventory.setMaxStackSize(1);

        updateInventory();
    }

    private void renderBackground() {
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        {
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName("§r");
            stack.setItemMeta(itemMeta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, stack);
        }
    }

    public boolean isValidItem(ItemStack stack) {
        return stack != null && !stack.getType().isAir() && getList().stream()
                .noneMatch(stack1 -> compareItem(stack1, stack));
    }

    /**
     * Compares the lower item with the higher item
     */
    public boolean compareItem(ItemStack higher, ItemStack lower) {
        boolean result = Objects.equals(higher.getType(), lower.getType());
        if (!result)
            return false;

        result = higher.getItemMeta() == null || lower.getItemMeta() != null;

        if (higher.getItemMeta() != null && result) {
            ItemMeta meta = higher.getItemMeta();

            result = !higher.getItemMeta().hasDisplayName() || lower.getItemMeta().hasDisplayName();

            if (meta.hasDisplayName() && result) {
                result = Objects.equals(meta.getDisplayName(), lower.getItemMeta().getDisplayName());
            }

            result = result && !higher.getItemMeta().hasLore() || lower.getItemMeta().hasLore();

            if (meta.hasLore() && result) {
                result = Objects.equals(meta.getLore(), lower.getItemMeta().getLore());
            }

            result = result && !higher.getItemMeta().hasCustomModelData() || lower.getItemMeta().hasCustomModelData();
            if (meta.hasCustomModelData() && result) {
                result = Objects.equals(meta.getCustomModelData(), lower.getItemMeta().getCustomModelData());
            }

            result = result && !higher.getItemMeta().hasAttributeModifiers() || lower.getItemMeta().hasAttributeModifiers();
            if (meta.hasAttributeModifiers() && result) {
                result = Objects.equals(meta.getAttributeModifiers(), lower.getItemMeta().getAttributeModifiers());
            }

            result = result && !higher.getItemMeta().isUnbreakable() || lower.getItemMeta().isUnbreakable();
        }
        return result;
    }

    public Set<ItemStack> getList() {
        return list;
    }

    public void updateInventory() {
        renderBackground();

        inventory.clear(inventory.getSize() - 5);
        inventory.setItem(inventory.getSize() - 3, DENY_ITEM);

        list.forEach(stack -> stack.setAmount(1));

        AtomicInteger i = new AtomicInteger();
        list.stream().limit(this.inventory.getSize() - 9)
                .forEach(stack -> inventory.setItem(i.getAndIncrement(), stack));

    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void save() throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("blacklist", this.list);
        configuration.save(file);
    }
}
