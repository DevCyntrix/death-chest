package com.github.devcyntrix.deathchest.blacklist;

import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
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

@Singleton
public class ItemBlacklist implements InventoryHolder {

    public static final ItemStack ADD_ITEM = new ItemStack(Material.EMERALD_BLOCK);

    public static final ItemStack FORCE_ADD_ITEM = new ItemStack(Material.LAPIS_BLOCK);

    public static final ItemStack DENY_ITEM = new ItemStack(Material.RED_STAINED_GLASS_PANE);

    public static final ItemStack NEXT_PAGE = new ItemStack(Material.ARROW);
    public static final ItemStack PREV_PAGE = new ItemStack(Material.ARROW);

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

        itemMeta = NEXT_PAGE.getItemMeta();
        itemMeta.setDisplayName("§7Next page");
        NEXT_PAGE.setItemMeta(itemMeta);

        itemMeta = PREV_PAGE.getItemMeta();
        itemMeta.setDisplayName("§7Previous page");
        PREV_PAGE.setItemMeta(itemMeta);

    }


    private final File file;
    @Getter
    private final Set<ItemStack> list = new HashSet<>();
    private final Inventory inventory;

    @Getter
    @Setter
    private int page;

    public ItemBlacklist(File file) {
        this.file = file;
        if (file.isFile()) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            this.list.addAll((Collection<? extends ItemStack>) yamlConfiguration.getList("blacklist", new ArrayList<>()));
        }
        this.list.removeIf(Objects::isNull);

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

    public void updateInventory() {
        renderBackground();

        inventory.clear(inventory.getSize() - 5);
        inventory.setItem(inventory.getSize() - 3, DENY_ITEM);

        list.stream()
                .filter(Objects::nonNull) // check for nonnull if you downgrade the server
                .forEach(stack -> stack.setAmount(1));

        AtomicInteger i = new AtomicInteger();
        list.stream()
                .skip((this.inventory.getSize() - 9L) * page)
                .limit(this.inventory.getSize() - 9L)
                .forEach(stack -> {
                    inventory.setItem(i.getAndIncrement(), stack);
                });


        if (getList().size() > (9 * 5 + 1) * (page + 1)) {
            getInventory().setItem(53, NEXT_PAGE);
        }

        if (page > 0) {
            getInventory().setItem(45, PREV_PAGE);
        }

    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void save() throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("blacklist", this.list.stream().toList());
        configuration.save(file);
    }
}
