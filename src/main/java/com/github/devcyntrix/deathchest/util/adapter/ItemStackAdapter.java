package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemStackAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> deserialize = context.deserialize(json, Map.class);
        return ItemStack.deserialize(deserialize);
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> serialize = src.serialize();
        return context.serialize(serialize);
    }
}
