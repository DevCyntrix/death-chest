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

import java.util.List;
import java.util.Objects;

public class ItemBlacklistListener implements Listener {

    private final ItemBlacklist blacklist;

    public ItemBlacklistListener(ItemBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @EventHandler
    public void onUpdateItem(InventoryChangeSlotItemEvent event) {
        if (!Objects.equals(event.getInventory().getHolder(), blacklist)) return;


        event.setCancelled(true);
        if (event.getSlot() == 9 * 6 - 5) {
            updateApplyItem(event.getInventory(), event.getTo());
            event.setCancelled(false);
        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!Objects.equals(blacklist, event.getClickedInventory().getHolder())) return;

        event.setCancelled(true);

        if (event.getSlot() < 9 * 6 - 9) {
            if (event.isRightClick()) {
                blacklist.getList().removeIf(itemStack -> itemStack.isSimilar(event.getCurrentItem()));
                blacklist.updateInventory();
                return;
            }

            // Blacklisted items
        } else {
            if (event.getSlot() == 53 && ItemBlacklist.NEXT_PAGE.isSimilar(event.getCurrentItem())) {
                blacklist.setPage(blacklist.getPage() + 1);
                blacklist.updateInventory();
                return;
            }
            if (event.getSlot() == 45 && ItemBlacklist.PREV_PAGE.isSimilar(event.getCurrentItem())) {
                blacklist.setPage(blacklist.getPage() - 1);
                blacklist.updateInventory();
                return;
            }

            if (event.getSlot() == 9 * 6 - 3) {
                ItemStack itemToAdd = event.getInventory().getItem(9 * 6 - 5);

                if (!blacklist.isValidItem(itemToAdd)) {
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
                    List<ItemStack> list = blacklist.getList().stream().filter(stack -> !blacklist.compareItem(itemToAdd, stack)).toList();
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


    private boolean hasDuplicate(ItemStack stack) {
        return stack != null && !stack.getType().isAir() && blacklist.getList().stream()
                .anyMatch(stack1 -> blacklist.compareItem(stack, stack1));
    }

    private void updateApplyItem(Inventory inventory, ItemStack clicked) {
        ItemStack stack = ItemBlacklist.DENY_ITEM;

        if (blacklist.isValidItem(clicked)) {
            stack = ItemBlacklist.ADD_ITEM;

            if (hasDuplicate(clicked)) {
                stack = ItemBlacklist.FORCE_ADD_ITEM;
            }
        }

        inventory.setItem(9 * 6 - 3, stack);
    }


}
