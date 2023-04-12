package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Type;

public class InventoryAdapter implements JsonSerializer<Inventory> {

    @Override
    public JsonElement serialize(Inventory src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", src.getType().name());
        object.addProperty("size", src.getSize());
        object.add("content", context.serialize(src.getContents()));
        return object;
    }
}
