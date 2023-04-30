package com.github.devcyntrix.deathchest.blacklist;

import com.github.devcyntrix.api.event.InventoryChangeSlotItemEvent;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class ItemBlacklistListener implements Listener {

    private final ItemBlacklist blacklist;

    public ItemBlacklistListener(ItemBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @EventHandler
    public void onUpdateItem(InventoryChangeSlotItemEvent event) {
        if (event.getInventory().getHolder() != blacklist) return;

        System.out.println(event.getSlot());
        System.out.println("FROM: " + event.getFrom());
        System.out.println("TO: " + event.getTo());


        event.setCancelled(true);
        if (event.getSlot() == 9 * 6 - 5) {
            updateApplyItem(event.getInventory(), event.getTo());
            event.setCancelled(false);
        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getHolder() != blacklist) return;

        event.setCancelled(true);


        if (event.getSlot() < 9 * 6 - 9) {
            // Blacklisted items
        } else {
            if (event.getSlot() == 9 * 6 - 3) {
                ItemStack itemToAdd = event.getInventory().getItem(9 * 6 - 5);

                if (!isValidItem(itemToAdd)) {
                    if (event.getWhoClicked() instanceof Player player) {
                        player.playNote(player.getLocation(), Instrument.BASS_DRUM, Note.flat(1, Note.Tone.D));
                    }
                    return;
                }


                if (ItemBlacklist.ADD_ITEM.isSimilar(event.getCurrentItem())) {
                    blacklist.getList().add(itemToAdd);

                    blacklist.updateInventory();
                    event.getInventory().clear(9 * 6 - 5);
                    updateApplyItem(event.getInventory(), null);
                    return;
                }

                if (ItemBlacklist.FORCE_ADD_ITEM.isSimilar(event.getCurrentItem())) {
                    List<ItemStack> list = blacklist.getList().stream().filter(stack -> !compareItem(itemToAdd, stack)).toList();
                    blacklist.getList().clear();
                    blacklist.getList().addAll(list);

                    event.getInventory().clear(9 * 6 - 5);
                    blacklist.getList().add(itemToAdd);
                    blacklist.updateInventory();
                    updateApplyItem(event.getInventory(), null);
                    return;
                }

            }

        }
    }

//    @EventHandler
//    public void onDrag(InventoryDragEvent event) {
//        if (event.getInventory() == null)
//            return;
//        if (event.getInventory().getHolder() != blacklist)
//            return;
//
//        long count = event.getRawSlots().stream()
//                .map(integer -> event.getView().getInventory(integer))
//                .filter(inventory -> inventory == event.getInventory())
//                .count();
//        System.out.println(count);
//        if (count <= 1) {
//            event.setCancelled(false);
//            return;
//        }
//        event.setCancelled(true);
//
//        if (event.getInventorySlots().contains(9 * 6 - 5)) {
//            if (event.getNewItems().size() > 1)
//                return;
//            event.setCancelled(false);
//
//            ItemStack stack = event.getNewItems().get(9 * 6 - 5);
//            updateApplyItem(event.getInventory(), stack);
//            return;
//        }
//
//    }

    /**
     * Compares the lower item with the higher item
     */
    private boolean compareItem(ItemStack higher, ItemStack lower) {
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

    private boolean isValidItem(ItemStack stack) {
        return stack != null && !stack.getType().isAir() && blacklist.getList().stream()
                .noneMatch(stack1 -> compareItem(stack1, stack));
    }

    private boolean hasDuplicate(ItemStack stack) {
        return stack != null && !stack.getType().isAir() && blacklist.getList().stream()
                .anyMatch(stack1 -> compareItem(stack, stack1));
    }

    private void updateApplyItem(Inventory inventory, ItemStack clicked) {
        ItemStack stack = ItemBlacklist.DENY_ITEM;

        if (isValidItem(clicked)) {
            stack = ItemBlacklist.ADD_ITEM;

            if (hasDuplicate(clicked)) {
                stack = ItemBlacklist.FORCE_ADD_ITEM;
            }
        }

        inventory.setItem(9 * 6 - 3, stack);
    }


}
