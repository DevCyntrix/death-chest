package com.github.devcyntrix.deathchest.api.menu;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;

public abstract class Window implements Closeable {

    private final Player player;
    private final Inventory handle;
    private final Set<GuiComponent> components = new HashSet<>();

    private InventoryView currentView;

    private boolean cancelled;

    private final Listener listener;


    public Window(Player player, String title, int rows) {
        this.player = player;
        this.handle = Bukkit.createInventory(null, rows * 9, title);
        this.listener = new Listener() {

            @EventHandler
            public void onClose(InventoryCloseEvent event) {
                if (!event.getInventory().equals(handle))
                    return;
                Window.this.currentView = null;
                Window.this.close();
            }

            @EventHandler
            public void onDrag(InventoryDragEvent event) {
                if (!event.getInventory().equals(handle))
                    return;
                Window.this.onDrag(event);
            }

            long lastInteraction;

            @EventHandler
            public void onClick(InventoryClickEvent event) {
                if (!event.getInventory().equals(handle))
                    return;
                if (event.getClickedInventory() == null)
                    return;
                if (event.isShiftClick() && event.getInventory().firstEmpty() != -1 && !handle.equals(event.getClickedInventory())) {
                    event.setCancelled(true);

                    Inventory clickedInventory = event.getClickedInventory();
                    if (event.getSlot() < 9) {
                        for (int i = 9; i < clickedInventory.getSize(); i++) {
                            if (clickedInventory.getItem(i) != null)
                                continue;
                            clickedInventory.setItem(i, event.getCurrentItem());
                            clickedInventory.setItem(event.getSlot(), null);
                            break;
                        }
                    } else {
                        for (int i = 0; i < 9; i++) {
                            if (clickedInventory.getItem(i) != null)
                                continue;
                            clickedInventory.setItem(i, event.getCurrentItem());
                            clickedInventory.setItem(event.getSlot(), null);
                            break;
                        }
                    }

                    return;
                }

                if (!handle.equals(event.getClickedInventory()))
                    return;
                if (lastInteraction + 150 > System.currentTimeMillis()) { // Avoid multiple clicks because of shift double left click
                    event.setCancelled(isCancelled());
                    return;
                }

                int x = event.getSlot() % 9;
                int y = event.getSlot() / 9;
                Window.this.onClick(new Position(x, y), event);
                lastInteraction = System.currentTimeMillis();
            }
        };

    }

    public abstract void initGui(Set<GuiComponent> components);

    public void onDrag(InventoryDragEvent event) {
        if (isCancelled()) {
            event.setCancelled(true);
        }
    }

    public void onClick(Position position, InventoryClickEvent event) {
        if (isCancelled()) {
            event.setCancelled(true);
        }

        components.forEach(component -> {
            if (position.x() < component.getPosition().x() || position.x() >= component.getPosition().x() + component.getDimension().columns())
                return;
            if (position.y() < component.getPosition().y() || position.y() >= component.getPosition().y() + component.getDimension().rows())
                return;
            Position relativePosition = new Position(position.x() - component.getPosition().x(), position.y() - component.getPosition().y());
            event.setCancelled(component.onClick(relativePosition, event, player, event.getInventory()));
        });
    }

    protected final boolean checkPositionAndDimension(Position position, Dimension dimension) {
        int originWidth = position.x() + dimension.columns();
        int originHeight = position.y() + dimension.rows();
        if (originWidth >= 9)
            return false;
        if (originHeight >= handle.getSize() / 9)
            return false;
        return true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public final Closeable open() {
        initGui(components);
        this.components.forEach(component -> component.render(handle, player));

        Bukkit.getPluginManager().registerEvents(listener, JavaPlugin.getPlugin(DeathChestPlugin.class));
        this.currentView = this.player.openInventory(handle);

        return this::close;
    }

    @Override
    public final void close() {
        if (currentView != null) {
            currentView.close();
        }
        components.clear();
        HandlerList.unregisterAll(listener);
    }
}
