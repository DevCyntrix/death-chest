package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemMetaAdapter implements JsonDeserializer<ItemMeta>, JsonSerializer<ItemMeta> {

    @Override
    public ItemMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> deserialize = context.deserialize(json, Map.class);
        return (ItemMeta) ConfigurationSerialization.deserializeObject(deserialize);
    }

    @Override
    public JsonElement serialize(ItemMeta src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> serialize = src.serialize();
        return context.serialize(serialize, Map.class);
    }
}
