package com.github.devcyntrix.deathchest.blacklist;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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


    private final Set<ItemStack> list = new HashSet<>();
    private final Inventory inventory;

    public ItemBlacklist(Set<ItemStack> list) {
        this.list.addAll(list);
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
}
