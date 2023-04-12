package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Type;
import java.util.UUID;

public class LivingEntityAdapter implements JsonSerializer<LivingEntity> {

    @Override
    public JsonElement serialize(LivingEntity src, Type typeOfSrc, JsonSerializationContext context) {
        UUID uniqueId = src.getUniqueId();
        String name = src.getName();
        JsonObject object = new JsonObject();
        object.add("id", context.serialize(uniqueId));
        object.addProperty("name", name);
        return object;
    }
}
