package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class InventoryAdapter implements JsonSerializer<Inventory> {

    @Override
    public JsonElement serialize(Inventory src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", src.getType().name());
        object.addProperty("size", src.getSize());
        object.add("content", context.serialize(Arrays.stream(src.getContents()).filter(Objects::nonNull).toArray(ItemStack[]::new)));
        return object;
    }
}
