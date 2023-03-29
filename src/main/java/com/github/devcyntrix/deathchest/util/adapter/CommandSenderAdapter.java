package com.github.devcyntrix.deathchest.util.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.lang.reflect.Type;

public class CommandSenderAdapter implements JsonSerializer<CommandSender> {

    @Override
    public JsonElement serialize(CommandSender src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("name", src.getName());
        object.addProperty("op", src.isOp());
        if (src instanceof Entity e) {
            object.add("id", context.serialize(e.getUniqueId()));
            object.addProperty("entity-id", e.getEntityId());
            object.add("location", context.serialize(e.getLocation()));
        } else if (src instanceof BlockCommandSender e) {
            object.add("location", context.serialize(e.getBlock().getLocation()));
            object.addProperty("type", e.getBlock().getBlockData().getAsString());
        }
        return object;
    }
}
