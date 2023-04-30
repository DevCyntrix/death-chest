package com.github.devcyntrix.api.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public class InventoryChangeSlotItemListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        InventoryView view = event.getView();

        int rawSlot = event.getRawSlot();
        Inventory inventory = view.getInventory(rawSlot);
        ItemStack previous = view.getItem(event.getRawSlot());
        ItemStack cursor = event.getCursor();

        int prevAmount = previous != null ? previous.getAmount() : 0;
        ItemStack newItem, oldItem = null;

        InventoryChangeSlotItemEvent itemEvent;

        System.out.println(event.getAction() + ": " + rawSlot + " " + event.getCurrentItem());

        switch (event.getAction()) {
            case PLACE_ONE:
                newItem = cursor.clone();
                newItem.setAmount(prevAmount + 1);

                itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, event.getSlot(), previous, newItem);
                itemEvent.setCancelled(event.isCancelled());
                Bukkit.getPluginManager().callEvent(itemEvent);
                event.setCancelled(itemEvent.isCancelled());
                break;
            case PLACE_ALL:
                newItem = cursor.clone();
                newItem.setAmount(cursor.getAmount() + prevAmount);

                itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, event.getSlot(), previous, newItem);
                itemEvent.setCancelled(event.isCancelled());
                Bukkit.getPluginManager().callEvent(itemEvent);
                event.setCancelled(itemEvent.isCancelled());
                break;
            case PICKUP_HALF:
                newItem = previous.clone();
                int amount = newItem.getAmount() / 2;
                if (amount > 0) {
                    newItem.setAmount(amount);
                } else newItem = null;

                itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, event.getSlot(), previous, newItem);
                itemEvent.setCancelled(event.isCancelled());
                Bukkit.getPluginManager().callEvent(itemEvent);
                event.setCancelled(itemEvent.isCancelled());
                break;
            case PICKUP_ALL:
                itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, event.getSlot(), previous, null);
                itemEvent.setCancelled(event.isCancelled());
                Bukkit.getPluginManager().callEvent(itemEvent);
                event.setCancelled(itemEvent.isCancelled());
                break;
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                itemEvent = new InventoryChangeSlotItemEvent(whoClicked, view.getBottomInventory(), event.getHotbarButton(), view.getBottomInventory().getItem(event.getHotbarButton()), null);
                Bukkit.getPluginManager().callEvent(itemEvent);

                if (!itemEvent.isCancelled()) {
                    itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, view.convertSlot(event.getRawSlot()), view.getItem(event.getRawSlot()), view.getBottomInventory().getItem(event.getHotbarButton()));
                    itemEvent.setCancelled(event.isCancelled());
                    Bukkit.getPluginManager().callEvent(itemEvent);
                    event.setCancelled(itemEvent.isCancelled());

                    System.out.println("2: " + itemEvent);
                }
                break;
            case SWAP_WITH_CURSOR:
                itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, event.getSlot(), previous, cursor);
                Bukkit.getPluginManager().callEvent(itemEvent);

                if (!itemEvent.isCancelled()) {
                    inventory.setItem(event.getSlot(), itemEvent.getTo());
                    event.setCursor(itemEvent.getFrom());
                }
                break;
            case DROP_ONE_SLOT:
            case DROP_ALL_SLOT:
            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
                break;
            case MOVE_TO_OTHER_INVENTORY:
                itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, event.getSlot(), previous, null);
                itemEvent.setCancelled(event.isCancelled());
                Bukkit.getPluginManager().callEvent(itemEvent);
                event.setCancelled(itemEvent.isCancelled());

                if (!itemEvent.isCancelled()) {
                    ItemStack currentItem = event.getCurrentItem();
                    if (currentItem == null)
                        return;

                    event.setCancelled(true);

                    Inventory otherInventory = view.getBottomInventory() == inventory ? view.getTopInventory() : view.getBottomInventory();
                    int spreadAmount = currentItem.getAmount();

                    for (int i = 0; i < otherInventory.getSize(); i++) {
                        if (spreadAmount <= 0)
                            break;

                        ItemStack item = otherInventory.getItem(i);
                        if (!currentItem.isSimilar(item) && item != null && !item.getType().isAir())
                            continue;

                        int maxStackSize = Math.min(currentItem.getMaxStackSize(), otherInventory.getMaxStackSize());
                        int currentAmount = 0;

                        if (item != null && !item.getType().isAir()) {
                            currentAmount = item.getAmount();
                            if (currentAmount >= maxStackSize)
                                continue;
                            oldItem = item.clone();
                        } else {
                            item = currentItem.clone();
                        }


                        int added = Math.min(maxStackSize - currentAmount, spreadAmount);
                        item.setAmount(currentAmount + added);

                        itemEvent = new InventoryChangeSlotItemEvent(whoClicked, otherInventory, i, oldItem, item);
                        Bukkit.getPluginManager().callEvent(itemEvent);
                        if (!itemEvent.isCancelled()) {
                            otherInventory.setItem(i, itemEvent.getTo());
                            spreadAmount -= added;
                        } else {
                            otherInventory.setItem(i, itemEvent.getFrom());
                        }
                    }

                    currentItem.setAmount(spreadAmount); // Sets the left amount
                    view.setItem(event.getRawSlot(), currentItem);

                }
                break;
        }

    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        InventoryView view = event.getView();
        Set<Integer> rawSlots = event.getRawSlots();

        int backAmount = 0;

        for (Integer rawSlot : rawSlots) {
            Inventory inventory = view.getInventory(rawSlot);
            Map<Integer, ItemStack> newItems = event.getNewItems();
            ItemStack oldItem = view.getItem(rawSlot);
            ItemStack newItem = newItems.get(rawSlot);
            InventoryChangeSlotItemEvent itemEvent = new InventoryChangeSlotItemEvent(whoClicked, inventory, view.convertSlot(rawSlot), oldItem, newItem);
            Bukkit.getPluginManager().callEvent(itemEvent);

            if (itemEvent.isCancelled()) {
                int oldAmount = oldItem != null ? oldItem.getAmount() : 0;
                backAmount += newItem.getAmount() - oldAmount;
            }
        }

        if (backAmount > 0) {
            ItemStack cursor = event.getCursor();
            int amount;
            if (cursor == null) {
                cursor = event.getOldCursor();
                amount = 0;
            } else {
                amount = cursor.getAmount();
            }
            cursor.setAmount(amount + backAmount);
            event.setCursor(cursor);
        }
    }

}
