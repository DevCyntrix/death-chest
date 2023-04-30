package com.github.devcyntrix.deathchest.api.menu;

import org.bukkit.inventory.ItemStack;

public abstract class Button extends GuiComponent {

    private ItemStack icon;

    public Button(Position position, Dimension dimension, ItemStack icon) {
        super(position, dimension);
        this.icon = icon;
    }

    @Override
    public void render(InventorySection section) {
        section.all(icon);
    }
}
